
; grunf | simple http monitoring loop

(ns grunf.bin
  "grunf.main"
  (:require [clj-http.client :as client]	
            [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.conversion :as qc]
            [org.httpkit.client :as http])
  (:use [clojurewerkz.quartzite.jobs :only [defjob]]
        [clojurewerkz.quartzite.schedule.simple
         :only [schedule with-repeat-count with-interval-in-milliseconds repeat-forever]]
        [clojure.tools.cli :only [cli]])
  (:gen-class))

(defn fetch
  "fetch given url"
  [url]
  (count (:body (client/get url))))

(defn fetchTime
  "get url fetch time"
  [url]
  (map #(Double/parseDouble %) (re-seq #"[0-9]+.[0-9]+" (with-out-str (time (fetch url))))))

(defjob FetchJob
	[ctx]
	(let [m (qc/from-job-data ctx)]
		(println (conj (fetchTime (get m "url")) (get m "url") (System/currentTimeMillis)))))

(defn submitFetchJob
  "submit fetch job for execution"
  [url interval]
  (let [job (j/build
             (j/of-type FetchJob)
             (j/using-job-data {"url" url})
             (j/with-identity (j/key (str "jobs.fetch." url) )))
        trigger (t/build
                 (t/with-identity (t/key (str "triggers." url) ))
                 (t/start-now)
                 (t/with-schedule (schedule
                                   (repeat-forever)
                                   (with-interval-in-milliseconds interval))))]
    (qs/schedule job trigger)))

(defn error
  [exception message]
  (println message))

(defn lets-puts-some-stress [url pressure]
  (let [last-repeat-time (atom (System/currentTimeMillis))
        bytes-received (atom 0)
        req-sent (atom 0)
        options {:timeout 2000
                 :user-agent "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1468.0 Safari/537.36" ;chrome
                 :headers {"X-Header" "value"}
                 :as :byte-array}]
    (println "entering stress test loop")
    (loop [sleep-interval 25
           pressure-segment (/ presure sleep-interval)]
      (dotimes [_ presure-segment]
        (http/get url options
                  (fn [{:keys [body error]}]
                    (when-not error
                      (swap! req-sent inc)
                      (swap! bytes-received + (count body))
                    ;; (printf "request success! req-sent: %d\n" @req-sent)
                    ))))
      (let [now (System/currentTimeMillis)]
        (if (-> now
                (- @last-repeat-time)
                (> 1000))
          (let [time (- now @last-repeat-time)
                throughput (-> @bytes-received
                               (/ time)
                               (* 1000.)
                               (/ 1024. 1024.))
                req-per-second (-> @req-sent
                                   (/ time)
                                   (* 1000.))]
            (printf "total requests %d, throughput: %.2fM/s, %.2f requests/seconds\n"
                    @req-sent throughput req-per-second)
            (reset! last-repeat-time now)
            (reset! req-sent 0)
            (reset! bytes-received 0)
            (flush))))
      (Thread/sleep sleep-interval)
      (recur))))
                      

(defn -main
  "Start Grunf. Pass hostnames/poll interval as (first argv)"
  [& argv]
  (let [[options args banner]
        (cli argv
             ["-c" "--config" "path to the config file"]
             ["-s" "--hosts" "list of host names spearated by commas" :parse-fn #(vec (.split % ","))]
             ["-t" "--rps" "requests per second" :parse-fn #(Integer. %)]
             ["--help" "print this help message" :default false :flag true])]
    (when (or (:help options) (nil? argv))
      (println banner)
      (System/exit 0))
    (try
      (qs/initialize)
      (qs/start)
      (let [[hosts interval]
            ((juxt :hosts :rps)
             (if-let [config-file (:config options)]
               (->> (slurp config-file)  ; TODO: No error handling when file not found
                    (read-string))       ; TODO: No error handling when parse error
               options))]
        (when (or (nil? hosts) (nil? interval))
          (println "must specify config file or '--hosts http://google.com,http://yahoo.com --interval 1000'")
          (System/exit 0))
        ;; (map #(submitFetchJob % interval) hosts)
        (lets-puts-some-stress (first hosts) interval)
        )
      (catch Exception e
        (println e)
        (error e "Error starting the app")))))
