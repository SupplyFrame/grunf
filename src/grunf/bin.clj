
; grunf | simple http monitoring loop

(ns grunf.bin
	"grunf.main"
	(:require [clj-http.client :as client]	
		  [clojurewerkz.quartzite.scheduler :as qs]
		  [clojurewerkz.quartzite.triggers :as t]
		  [clojurewerkz.quartzite.jobs :as j]
		  [clojurewerkz.quartzite.conversion :as qc])
	(:use [clojurewerkz.quartzite.jobs :only [defjob]]
	      [clojurewerkz.quartzite.schedule.simple :only [schedule with-repeat-count with-interval-in-milliseconds repeat-forever]]
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

(defn -main
  "Start Grunf. Pass hostnames/poll interval as (first argv)"
  [& argv]
  (let [[options args banner]
        (cli argv
             ["-c" "--config" "path to config file"]
             ["-s" "--hosts" "list of host names spearated by commas" :parse-fn #(vec (.split % ","))]
             ["-t" "--interval" "poll interval" :parse-fn #(Integer. %)]
             ["--help" "print this help message" :default false :flag true])]
    (when (or (:help options) (nil? argv))
      (println banner)
      (System/exit 0))
    (try
      (qs/initialize)
      (qs/start)
      (let [[hosts interval]
            (if-let [config-file (:config options)]
              (->> (slurp config-file)
                   (read-string)
                   ((juxt :hosts :interval)))
              ((juxt :hosts :interval) options))]
        (when (or (nil? hosts) (nil? interval))
          (println "must specify config file or '--hosts http://google.com,http://yahoo.com --interval 1000'")
          (System/exit 0))
        (map #(submitFetchJob % interval) hosts))
      (catch Exception e
        (println e)
        (error e "Error starting the app")))))
