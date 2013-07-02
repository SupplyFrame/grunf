[{
  :url "http://www.google.com"
  ;;:interval 50000
  ;;:validator #(re-find #"yahoo" %) ;; will show error :)
  :http-options {:timeout 2000
                 :user-agent "Mozilla"}
  :params-fn (map #(hash-map :id %) (iterate inc 0))
  ;; :graphite-ns "com.yahoo.www"
  ;; explicitly setup graphite namespace

  ;; sub namespace
  ;; graphite namespace would be "com.google.www.hoho"
  }
]
