(ns grunf.core
  "gurnf.core"
  (:require [org.httpkit.client :as http]))

(defprotocol GrunfOutputAdapter
  "A protocol for grunf instance to log or push data to other service"
  (log-success [this] "http 2xx status")
  (log-redirect [this] "http 3xx status")
  (log-client-error [this] "http 4xx status")
  (log-server-error [this] "http 5xx status")
  (log-validate-error [this] "validation failed")
  (log-unknown-error [this] "link error or unknown status code"))

(defn- http-method
  "take http-method names and return actual instance"
  [method]
  (case method
    :get http/get
    :post http/post
    :put http/put
    :delete http/delete))

(defn fetch [{:keys [url interval method http-options validator graphite-ns]
              :or {interval 5000,
                   method :get,
                   validator '(constantly true)
                   graphite-ns ""}
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
    (let [validator-exec (eval validator)]
      (loop [start (System/currentTimeMillis)]
        ((http-method method) url (assoc http-options
                                    ;;:timeout 60000
                                    ;;:user-agent "Grunf"
                                    :validator validator-exec
                                    :validator-source validator
                                    :as :text
                                    :start start) callback)
        (Thread/sleep interval)
        (recur (System/currentTimeMillis))))))
