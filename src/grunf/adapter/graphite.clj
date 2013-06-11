(ns grunf.adapter.graphite
  "graphite adapter"
  (:require [clojure.string :as str])
  (:use clojure.template
        [grunf.core :only [GrunfOutputAdapter]])
  (:import [java.net Socket]
           [java.io PrintWriter]))

(deftype Graphite [namespace host port]
  GrunfOutputAdapter
  (log-success [this]
    (fn [{{start :start} :opts
          body :body}]
      (binding [*out* (-> (Socket. (.host this) (.port this))
                          (.getOutputStream)
                          (PrintWriter.))]
        (do-template [type value]
                     (println (str (.namespace this) "." type)
                              value
                              (-> start (/ 1000) (int)))
                     "response_time" (- (System/currentTimeMillis) start)
                     "response_size" (count (map int body))
                     "error" 0
                     ))))
  (log-validate-error [this]
    (fn [{{start :start} :opts
          body :body}]
      (binding [*out* (-> (Socket. (.host this) (.port this))
                          (.getOutputStream)
                          (PrintWriter.))]
        (println (str (.namespace this) ".error") 1 (-> start (/ 1000) (int))))))
  (log-redirect [this] (fn [_]))
  (log-client-error [this] (grunf.core/log-unknown-error this))
  (log-server-error [this] (grunf.core/log-unknown-error this))
  (log-unknown-error [this]
    (fn [{{start :start} :opts}]
      (binding [*out* (-> (Socket. (.host this) (.port this))
                          (.getOutputStream)
                          (PrintWriter.))]
        (println (str (.namespace this) ".error") 1 start)))))
