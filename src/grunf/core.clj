(ns grunf.core
  "gurnf.core"
  (:require [org.httpkit.client :as http])
  (:use [clojure.template :only [do-template]]
        clojure.tools.logging
        clj-logging-config.log4j
        )
  (:import [java.net Socket]
           [java.io PrintWriter]
           [org.apache.log4j DailyRollingFileAppender EnhancedPatternLayout]))


(set-logger! :level :debug
             :out (DailyRollingFileAppender.
                   (EnhancedPatternLayout. EnhancedPatternLayout/TTCC_CONVERSION_PATTERN)
                   "logs/foo.log"
                   "'.'yyyy-MM-dd")
             )

(defn- now [] (System/currentTimeMillis))

(defn- to-sec [milisec]
  (-> milisec (/ 1000) (int)))

(defn- write-graphite [log]
  (binding [*out* (-> (Socket. "localhost" 2003)
                      (.getOutputStream)
                      (PrintWriter.))]
    (println log)))

(defn- log-graphite [name value timestamp]
  (let [log (clojure.string/join " "
                                 [(str "grunf." name)
                                  value
                                  timestamp])]
    (info log)
    (write-graphite log)))



(defn fetch [{:keys [name url http-options interval validator redirect]}]
  (loop []
    (let [start (now)]
      (http/get url (assoc http-options :as :text)
                (fn [{:keys [status headers body error opts]}]
                  (if (or error (not ((eval validator)
                                      body)))
                    (log-graphite (str name ".error") 1 (to-sec start))
                    (do-template
                     [type value]
                     (log-graphite (str name type) value (to-sec start))
                     ".error" 0
                     ".response_time" (-> (now)
                                          (- start)
                                          (/ 1000.))
                     ".response_size" (count (map int body))))))
      (Thread/sleep interval)
      (recur))))



;; Need an util function to print, log, and flush
                    
                    
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
           pressure-segment (/ pressure sleep-interval)]
      (dotimes [_ pressure-segment]
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
      (recur sleep-interval pressure-segment))))
