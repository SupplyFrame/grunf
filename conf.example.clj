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

[{
  :url "http://www.google.com"
  :interval 5000
  :validator #(re-find #"google" %)
  :http-options {:timeout 2000
                 :user-agent "Mozilla"}
  }
 {:url "http://yahoo.com"}
]

;; {:hosts ["http://google.com" "http://yahoo.com"]
;;  :interval 1000}


;; TODO: logger config
