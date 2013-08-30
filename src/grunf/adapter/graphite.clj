(ns grunf.adapter.graphite
  "graphite adapter"
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log])
  (:use clojure.template
        [grunf.core :only [GrunfOutputAdapter pool]])
  (:import [java.net Socket ConnectException]
           [java.io PrintWriter]
           [java.util.concurrent ThreadPoolExecutor TimeUnit ArrayBlockingQueue ThreadPoolExecutor$DiscardOldestPolicy]
))


(defmacro with-socket [[sym socket] & body]
  `(with-open [sck# (.getOutputStream ~socket)]
     (binding [~sym (PrintWriter. sck#)]
       ~@body)))

(deftype Graphite [namespace host port]
  GrunfOutputAdapter
  (log-success [this]
    (fn [{{start :start} :opts
          body :body}]
      (let [diff (- (System/currentTimeMillis) start)]
        (.execute pool
                  (fn []
                    (try
                      (with-socket [*out* (Socket. (.host this) (.port this))]
                        (do-template [type value]
                                     (println (str (.namespace this) "." type)
                                              value
                                              (-> start (/ 1000) (int)))
                                     "response_time" diff
                                     "response_size" (count (map int body))
                                     "error" 0))
                      (catch ConnectException e
                        (log/error (str  "Cannot connect to graphite server") (.host this) "port:" (.port this)))))))))
  (log-validate-error [this]
    (fn [{{start :start} :opts
          body :body}]
      (.execute pool
                (fn []
                  (try
                    (with-socket [*out* (Socket. (.host this) (.port this))]
                      (println (str (.namespace this) ".error") 1 (-> start (/ 1000) (int))))
                    (catch ConnectException e
                      (log/error (str  "Cannot connect to graphite server") (.host this) (.port this))))))))
  (log-redirect [this] (fn [_]))
  (log-client-error [this] (grunf.core/log-unknown-error this))
  (log-server-error [this] (grunf.core/log-unknown-error this))
  (log-unknown-error [this]
    (fn [{error :error
          status :status
          headers :headers
          {url :url start :start} :opts}]
      (.execute pool
                (fn []
                  (try
                    (with-socket [*out* (Socket. (.host this) (.port this))]
                      (println (str (.namespace this) ".error") 1 (-> (System/currentTimeMillis) (/ 1000) (int))))
                    (catch ConnectException e
                      (log/error (str  "Cannot connect to graphite server") (.host this) (.port this)))))))))
