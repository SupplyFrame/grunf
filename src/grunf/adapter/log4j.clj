(ns grunf.adapter.log4j
  "log4j adapter"
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log])
  (:use clj-logging-config.log4j
        [grunf.core :only [GrunfOutputAdapter]]))


(deftype Log4j [])

(extend-type Log4j
  GrunfOutputAdapter
  (log-success [this]
    (fn [{{v-source :validator-source validator :validator url :url start :start} :opts
          status :status
          body :body}]
      (log/info status url
                "response time (msec):" (- (System/currentTimeMillis) start))))
  (log-validate-error [this]
    (fn [{{v-source :validator-source validator :validator url :url start :start} :opts
          status :status
          body :body}]
      (log/error status url
                 "response time (msec):" (- (System/currentTimeMillis) start) ","
                 "-- validate failed -- validator:" v-source)))
  (log-redirect [this]
    (fn [{{old-url :url} :opts
          {new-url :location} :headers
          status :status}]
      (log/info status "redirect" old-url "->" new-url)))
  (log-unknown-error [this]
    (fn [{error :error
          status :status
          headers :headers
          {url :url} :opts}]
      (log/error status url error headers)))
  (log-client-error [this] (grunf.core/log-unknown-error this))
  (log-server-error [this] (grunf.core/log-unknown-error this)))
