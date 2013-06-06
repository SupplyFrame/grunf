(ns grunf.core
  "gurnf.core"
  (:require [org.httpkit.client :as http]
            [clojure.string :as str]
            [clojure.tools.logging :as log])
  (:use [clojure.template :only [do-template]]
        clj-logging-config.log4j)
  (:import [java.net Socket]
           [java.io PrintWriter]
           [org.apache.log4j DailyRollingFileAppender EnhancedPatternLayout]))

(set-logger! :level :debug
             :out (DailyRollingFileAppender.
                   (EnhancedPatternLayout. "%d{ISO8601}{GMT} [%-5p] [%t] - %m%n")
                   "logs/foo.log"
                   "'.'yyyy-MM-dd")
             )

(defn set-graphite! []) ;; set by main args
(defn set-log4j! [])    ;; set by main args
                        ;; May become thread local in the future

(declare now to-sec write-graphite log-graphite private-fetch)
(def ^:dynamic *graphite-ns*)
(def ^:dynamic *validator*)
(def ^:dynamic *start*)

(defmulti callback
  "dispatch fetch callbacks base on connection error or http status"
  (fn [{:keys [status error]}]
    (if error :error
        (quot status 100)
          2 :success
          3 :redirect
          :error))))

(defmethod callback :error [{:keys [error status opts headers]}]
  (log/error status (:url opts) error headers)
  (binding [*out* (-> (Socket. "localhost" 2003)
                      (.getOutputStream)
                      (PrintWriter.))]
    (println (str *graphite-ns* ".error") 1 (-> (:start opts) (/ 1000) (int)))))

(defmethod callback :success [{:keys [status body opts]}]
  (log/info status (:url opts))
  (when-not ((:validator opts) body) (log/error status (:url opts) "-- validate failed"))
  (binding [*out* (-> (Socket. "localhost" 2003)
                      (.getOutputStream)
                      (PrintWriter.))]
    (do-template
     [type value]
     (println (str (:graphite-ns opts) "." type)
              value
              (-> (:start opts) (/ 1000) (int)))
     "response_time" (- (System/currentTimeMillis) (:start opts))
     "response_size" (count (map int body))
     "error" (if ((:validator opts) body) 0 1))))

(defmethod callback :redirect [{:keys [status headers opts]}]
  (log/info status "redirect" (:url opts) "->" (:location headers))
  (private-fetch (:location headers) (:method opts) opts))



;; names need to be refactored
(defn- private-fetch [url method http-options]
  ((case method
     :get http/get
     :post http/post
     :put http/put
     :delete http/delete) url http-options callback))

(defmacro ->>split
  "Thread last friendly split"
  [sep form]
  `(clojure.string/split ~form ~sep))

(defn- url->rev-host
  "Resove host, than reverse the order
   http://www.yahoo.com/search.html -> com.yahoo.www"
  [url]
  (->> url
       (re-find #"(?<=://).+?(?=/|$)")
       (->>split #"\.")
       (reverse)
       (str/join ".")))


;; predicate instead of validator?
(defn fetch
  "not documented yet"
  [{:keys [url interval method http-options validator graphite-ns]
    :or {interval 5000, method :get}}]
    (loop []
      (let [graphite-ns (if graphite-ns graphite-ns (url->rev-host url) )              
            validator (if validator (eval validator) #(constantly true))
            start (System/currentTimeMillis)]
      (private-fetch url method
                     (assoc http-options
                       :validator validator
                       :graphite-ns graphite-ns
                       :start start))
      (Thread/sleep interval)
      (recur))))
