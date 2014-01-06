[{
  :riemann-service "fcl"
  :url "http://192.168.0.1/search/TR110"
  :interval 10000
  :http-options {:timeout 2000
                 :user-agent "Grunf 0.4.02"}
  :riemann-tags ["1w" "findchips.com"]
  }
 {
  :riemann-service "search-soa"
  :url "http://192.168.0.1:8090/search-soa/search/pF/0/1?search=max232"
  :method :put
  :headers {"Content-Type" "application/json"
            "Accept" "application/json"}
  :interval 10000
  :http-options {:timeout 2000
  		 :http-method put
                 :user-agent "Grunf 0.4.02"}
  :riemann-tags ["1w" "search-soa"]
  }
 {
  :riemann-service "esp"
  :url "http://192.168.0.1:9080/solr/esp/search?search=analog&linkFilter=ESP&partnerName=abc"
  :interval 10000
  :http-options {:timeout 2000
                 :user-agent "Grunf 0.4.02"}
  :riemann-tags ["1w" "esp"]
  }
 {
  :riemann-service "projects.hd.com"
  :url "http://alpha.projects.hd.com"
  :interval 10000
  :http-options {:timeout 2000
                 :user-agent "Grunf 0.4.02"}
  :riemann-tags ["1w" "projects.hd.com"]
  }
]
