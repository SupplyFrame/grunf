(ns grunf.adapter.csv
  "csv adapter"
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log])
  (:use clj-logging-config.log4j
        [grunf.core :only [GrunfOutputAdapter]]))

(deftype CSV [])

(extend-type CSV
  GrunfOutputAdapter
  (log-success [this]
    (fn [{{v-source :validator-source validator :validator url :url start :start} :opts
          status :status
          body :body}]
      (log/info (str status "," url "," (- (System/currentTimeMillis) start)))))
  (log-validate-error [this]
    (fn [{{v-source :validator-source validator :validator url :url start :start} :opts
          status :status
          body :body}]
      (log/error (str status "," url "," (- (System/currentTimeMillis) start)))))
  (log-redirect [this] (fn [_]))
  (log-unknown-error [this]
    (fn [{error :error
          status :status
          headers :headers
          {url :url start :start} :opts}]
      (log/error (str status "," url "," (- (System/currentTimeMillis) start)))))
  (log-client-error [this] (grunf.core/log-unknown-error this))
  (log-server-error [this] (grunf.core/log-unknown-error this)))

