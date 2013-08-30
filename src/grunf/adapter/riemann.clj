(ns grunf.adapter.riemann
  "riemann adapter"
  (:use riemann.client
        [grunf.core :only [GrunfOutputAdapter pool]])
  (:import [java.io IOException]))

(deftype RiemannAdapter [client tags]
  GrunfOutputAdapter
  (log-success [this]
    (fn [{{start :start
           url :url
           ttl :interval} :opts}]
      (let [now (int (/ (System/currentTimeMillis) 1000))
            diff (- (System/currentTimeMillis) start)]
        (.execute pool
                  (fn []
                    (try
                      (send-event (.client this)
                                  {:service url
                                   :state "ok"
                                   :time now
                                   :tags (merge tags "grunf")
                                   :description "query time"
                                   :metric diff
                                   :ttl (/ (* ttl 5) 1000)
                                   })
                      (catch IOException e)))))))
  (log-validate-error [this]
    (fn [{{start :start
           url :url
           ttl :interval} :opts}]
      (let [now (int (/ (System/currentTimeMillis)))
            diff (- (System/currentTimeMillis) start)]
        (.execute pool
                  (fn []
                    (try (send-event (.client this)
                                     {:service url
                                      :state "warning"
                                      :time now
                                      :tags (merge tags "grunf")
                                      :description "validation error"
                                      :metric diff
                                      :ttl (/ (* ttl 5) 1000)
                                      })
                         (catch IOException e)))))))
  (log-redirect [this] (fn [_]))
  (log-client-error [this] (grunf.core/log-unknown-error this))
  (log-server-error [this] (grunf.core/log-unknown-error this))
  (log-unknown-error [this]
    (fn [{{start :start
           url :url
           ttl :interval} :opts
           error :error
           status :status}]
      (let [now (int (/ (System/currentTimeMillis)))
            diff (- (System/currentTimeMillis) start)]
        (.execute pool
                  (fn []
                    (try (send-event (.client this)
                                     {:service url
                                      :state "error"
                                      :time (int (/ (System/currentTimeMillis) 1000))
                                      :tags (merge tags "grunf")
                                      :description (str status ", error:" error)
                                      :metric (- (System/currentTimeMillis) start)
                                      :ttl (/ (* ttl 2) 1000)
                                      })
                         (catch IOException e)))))))
  )

