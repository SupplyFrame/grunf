[{
  :url "http://www.google.com"
  :interval 50000
  :validator #(re-find #"yahoo" %)
  :http-options {:timeout 2000
                 :user-agent "Mozilla"}
  ;; :graphite-ns "com.yahoo.www"
  ;; explicitly setup graphite namespace

  :name "hoho"
  ;; sub namespace
  ;; graphite namespace would be "com.google.www.hoho"
  }
 {
  :url "http://google.com"
  }
]
