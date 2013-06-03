(ns grunf.core
  "gurnf.core"
  (:require [org.httpkit.client :as http])
  (:import [java.net Socket]
           [java.io PrintWriter]))

(defn- now []
  (int (/ (System/currentTimeMillis) 1000)))

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
;;    (println log)
    (write-graphite log)))



(defn fetch [{:keys [name url http-options interval validator redirect]}]
  (loop []
    (let [start (now)]
      (http/get url (assoc http-options :as :text)
                (fn [{:keys [status headers body error opts]}]
                  (if error
                    (log-graphite (str name ".error") 1 start)
                    (do
                      (log-graphite (str name ".error") 0 start)
                      (log-graphite (str name ".response_time") (- (now) start) start)
                      (log-graphite (str name ".response_size") (count (map int body)) start)))))
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
