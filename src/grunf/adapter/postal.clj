(ns grunf.adapter.postal
  (:use postal.core
        grunf.core))

(deftype Mail [options hostname])

(extend-type Mail
  GrunfOutputAdapter
  (log-success [this] (fn [_]))
  (log-validate-error [this]
    (fn [{{validator :validator-source url :url start :start} :opts
          status :status
          body :body}]
      ;;(println (meta (.options this)) (.options this))
      (send-message
       (assoc (.options this)
         :subject (str "Grunf validate page <" url "> from " (.hostname this) " failed")
         :body (with-out-str
                 (println "Grunf validate page <" url "> from" (.hostname this) "failed")
                 (println "--------------------------------------------")
                 (println status "response time (msec):" (- (System/currentTimeMillis) start))
                 (println "validator: " validator)
                 (println "--------------------------------------------")
                 (println "response body:\n")
                 (println body))))))
  (log-redirect [this] (fn [_]))
  (log-unknown-error [this]
    (fn [{error :error
          status :status
          headers :headers
          {url :url start :start validator :validator-source} :opts}]
      (send-message
       (assoc (.options this)
         :subject (str "Grunf validate page <" url "> from " (.hostname this) " failed")
         :body (with-out-str
                 (println "Grunf validate page <" url "> from" (.hostname this) "failed")
                 (println "--------------------------------------------")
                 (println status "response time (msec):" (- (System/currentTimeMillis) start))
                 (println "validator: " validator)
                 (println "--------------------------------------------")
                 (println "headers")
                 (println headers)
                 (println "--------------------------------------------")
                 (println "error message")
                 (println error))))))
  (log-client-error [this] (grunf.core/log-unknown-error this))
  (log-server-error [this] (grunf.core/log-unknown-error this)))
