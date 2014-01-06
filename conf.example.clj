[{
  :service "fcl"
  :url "http://198.54.96.98/search/TR110"
  :interval 10000
  :http-options {:timeout 2000
                 :user-agent "Grunf 0.4.02"}
  :riemann-tags ["1w" "findchips.com"]
  }
 {
  :service "search-soa"
  :url "http://198.54.96.98:8090/search-soa/search/partFamily/0/1?search=max232"
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
  :service "sep"
  :url "http://198.54.96.98:9080/solr/sep/search?search=analog&linkFilter=SEP&partnerName=123"
  :interval 10000
  :http-options {:timeout 2000
                 :user-agent "Grunf 0.4.02"}
  :riemann-tags ["1w" "sep"]
  }
 {
  :service "projects.hackaday.com"
  :url "http://alpha.projects.hackaday.supplyframe.com"
  :interval 10000
  :http-options {:timeout 2000
                 :user-agent "Grunf 0.4.02"}
  :riemann-tags ["1w" "projects.hackaday.com"]
  }
]
