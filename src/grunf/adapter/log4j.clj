(ns grunf.adapter.log4j
  "log4j adapter"
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log])
  (:use clj-logging-config.log4j
        [grunf.core :only [GrunfOutputAdapter]]))


(defprotocol SetupLog4j
  "A protocol that Log4j needed"
  (init-logger [this] "runs set-loggers!"))

(defmacro with-log4j [this & body] ;; It should create custom namespace
  `(with-logging-config
     [(.namespace ~this) {:level (.level ~this)}]
     ~@body))

(deftype Log4j [namespace level pattern out])

(extend-type Log4j
  SetupLog4j
  (init-logger [this]
    (set-loggers! (.namespace this)
                  {:level (.level this)
                   :pattern (.pattern this)
                   :out (.out this)}))
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
