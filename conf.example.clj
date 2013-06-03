;; [{
;;   :host "http://yahoo.com"
;;   :interval 1000
;;   :validate (partial re-find #"google")
;;   :http-options {
;;                  :timeout 2000
;;                  :user-agent "Mozilla"
;;                  :headers {"X-Header" "Value"}}
;;   :redirect True

;;   }]

[{:name "yahoo"
  :url "http://www.yahoo.com/"
  :meta {:from "0.0.0.0"}
  :interval 1000
  :validator #(re-find #"yahoo" %)
  :http-options {:timeout 2000
                 :user-agent "Mozilla"}
  :redirect True
  }]

;; {:hosts ["http://google.com" "http://yahoo.com"]
;;  :interval 1000}


;; TODO: logger config
