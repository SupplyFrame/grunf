(ns grunf.core
  "gurnf.core"
  (:require [org.httpkit.client :as http]
            [clojure.string :as str]
            )
  (:use overtone.at-at)
  (:import [java.net URLEncoder]
           [java.util.concurrent
            TimeUnit ThreadPoolExecutor
            ArrayBlockingQueue ThreadPoolExecutor$DiscardOldestPolicy]))

(defprotocol GrunfOutputAdapter
  "A protocol for grunf instance to log or push data to other service"
  (log-success [this] "http 2xx status")
  (log-redirect [this] "http 3xx status")
  (log-client-error [this] "http 4xx status")
  (log-server-error [this] "http 5xx status")
  (log-validate-error [this] "validation failed")
  (log-unknown-error [this] "link error or unknown status code"))

(defonce at-at-pool (mk-pool))
(defonce pool (ThreadPoolExecutor.
               4                        ; 4 core threads
               20 ; Takes 10mb memory size, and opens 20 sockets at most.
               180 TimeUnit/SECONDS      ; keep alive for 3 min
               (ArrayBlockingQueue. 200) ; queue for 200 jobs
               (ThreadPoolExecutor$DiscardOldestPolicy.) ; discard oldest jobs if the queue is out of space
               ))

(defn- http-method
  "take http-method names and return actual instance"
  [method]
  (case method
    :get http/get
    :post http/post
    :put http/put
    :delete http/delete))

(defn- url-encode [s] (URLEncoder/encode (str s) "utf8"))

(defn- query-string
  "Returns URL-encoded query string for given params map."
  [m]
  (let [param (fn [k v]  (str (url-encode (name k)) "=" (url-encode v)))
        join  (fn [strs] (str/join "&" strs))]
    (join (for [[k v] m] (if (sequential? v)
                           (join (map (partial param k) (or (seq v) [""])))
                           (param k v))))))



(defn fetch [{:keys [url interval method http-options validator graphite-ns params-fn]
              :or {interval 5000,
                   method :get,
                   validator '(constantly true)
                   graphite-ns ""
                   params-fn '(repeat nil)}
              :as input-options}
             adapters]
  (letfn
      [(callback
         [{:keys [error status body opts] :as context}]
         (let [log-wrapper (fn [f] ((apply juxt (map f adapters)) context))]
           (if error
             (log-wrapper log-unknown-error)
             (case (quot status 100)
               2 (if-let [v (:validator opts)]
                   (try (if (v body)
                          (log-wrapper log-success)
                          (log-wrapper log-validate-error))
                        (catch Exception e
                          (log-wrapper log-validate-error)))
                   (log-wrapper log-success))
               3 (do (log-wrapper log-redirect)
                     ((http-method method)
                      (-> context :headers :location) opts callback))
               4 (log-wrapper log-client-error)
               5 (log-wrapper log-server-error)
               (log-wrapper log-unknown-error)))))]
    
    (let [validator (eval validator)
          params-seq (atom (eval params-fn))]
      (every interval
             (fn []
               (let [start (System/currentTimeMillis)
                     url (if (first @params-seq) ; url
                           (if (neg? (.indexOf ^String url (int \?)))
                             (str url "?" (query-string (first @params-seq)))
                             (str url "&" (query-string (first @params-seq))))
                           url)]
                 ((http-method method)
                  url
                  (assoc http-options   ; http-options
                    :validator validator
                    :validator-source validator
                    :interval interval
                    :as :text
                    :start start)
                  callback)
                 (swap! params-seq rest)))
             at-at-pool))))


