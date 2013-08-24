[{
  :url "http://www.yahoo.com"
  :interval 50000
  :http-options {:timeout 2000
                 :user-agent "Mozilla"}
  :riemann-tags ["Yahoo" "Homepage"]
  }
 {
  :url "http://google.com"
  :riemann-tags ["Google" "Homepage"]
  }
]
