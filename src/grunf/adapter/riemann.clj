(ns grunf.adapter.riemann
  "riemann adapter"
  (:use riemann.client
        [grunf.core :only [GrunfOutputAdapter]])
  (:import [java.io IOException]))

(deftype RiemannAdapter [client]
  GrunfOutputAdapter
  (log-success [this]
    (fn [{{start :start
           url :url
           ttl :interval} :opts}]
      (try
        (send-event (.client this)
                    {:service url
                     :state "ok"
                     :time (int (/ (System/currentTimeMillis) 1000))
                     :tags ["grunf"]
                     :description "query time"
                     :metric (- (System/currentTimeMillis) start)
                     :ttl (/ (* ttl 2) 1000)
                     })
        (catch IOException e))))
  (log-validate-error [this]
    (fn [{{start :start
           url :url
           ttl :interval} :opts}]
      (try (send-event (.client this)
                       {:service url
                        :state "warning"
                        :time (int (/ (System/currentTimeMillis) 1000))
                        :tags ["grunf"]
                        :description "validation error"
                        :metric (- (System/currentTimeMillis) start)
                        :ttl (/ (* ttl 2) 1000)
                        })
           (catch IOException e))))
  (log-redirect [this] (fn [_]))
  (log-client-error [this] (grunf.core/log-unknown-error this))
  (log-server-error [this] (grunf.core/log-unknown-error this))
  (log-unknown-error [this]
    (fn [{{start :start
           url :url
           ttl :interval} :opts
           error :error
           status :status}]
      (try (send-event (.client this)
                       {:service url
                        :state "error"
                        :time (int (/ (System/currentTimeMillis) 1000))
                        :tags ["grunf"]
                        :description (str status ", error:" error)
                        :metric (- (System/currentTimeMillis) start)
                        :ttl (/ (* ttl 2) 1000)
                        })
           (catch IOException e))))
  )

