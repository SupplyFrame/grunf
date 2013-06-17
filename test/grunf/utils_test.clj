(ns grunf.utils-test
  (:use clojure.test
        grunf.utils
        grunf.adapter.graphite)
  (:import [grunf.adapter.graphite Graphite]))

(deftest test-urls

  (testing "No sub domains"
    (is (= "com.yahoo"
           (url->rev-full "http://yahoo.com")))
    (is (= "com.yahoo"
           (url->rev-full "https://yahoo.com")))
    )
  (testing "reverse domain name"
    (is (= "com.yahoo.www"
           (url->rev-full "http://www.yahoo.com"))))
  (testing "reverse domain and combine sub-directories"
    (is (= "com.stackoverflow.questions-944446-what-is-point-free-style-in-functional-programming"
           (url->rev-full "http://stackoverflow.com/questions/944446/what-is-point-free-style-in-functional-programming")))
    (is (= "com.github.SupplyFrame-grunf-wiki-graphite-on-ubuntu"
           (url->rev-full "https://github.com/SupplyFrame/grunf/wiki/graphite-on-ubuntu"))))

  (testing "long params"
    (is (= "com.google.www.url#satrc>>>8daGc"
           (url->rev-full "http://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&ved=0CCwQFjAA&url=http%3A%2F%2Fen.wikipedia.org%2Fwiki%2FFelix&ei=P3e_UYbTG4iuiAfggoC4Bg&usg=AFQjCNGStDFlon-pIWjSn3gOLdD760PJbw&bvm=bv.47883778,d.aGc")))))

(deftest graphite
  (def mk-graphite (with-graphite-global {:graphite-host "localhost" :graphite-port 2003}))
  (testing "No sub domains"
    (is (= "com.yahoo"
           (.namespace (mk-graphite {:url "http://yahoo.com"}))))
    (is (= "com.yahoo"
           (.namespace (mk-graphite {:url "https://yahoo.com"})))))
  (testing "reverse domain name"
    (is (= "com.yahoo.www"
           (.namespace (mk-graphite {:url "http://www.yahoo.com"})))))
  (testing "reverse domain and combine sub-directories"
    (is (= "com.stackoverflow.questions-944446-what-is-point-free-style-in-functional-programming"
           (.namespace (mk-graphite {:url "http://stackoverflow.com/questions/944446/what-is-point-free-style-in-functional-programming"}))))
    (is (= "com.github.SupplyFrame-grunf-wiki-graphite-on-ubuntu"
           (.namespace (mk-graphite {:url "https://github.com/SupplyFrame/grunf/wiki/graphite-on-ubuntu"})))))

  (testing "long params"
    (is (= "com.google.www.url#satrc>>>8daGc"
           (.namespace (mk-graphite {:url "http://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&ved=0CCwQFjAA&url=http%3A%2F%2Fen.wikipedia.org%2Fwiki%2FFelix&ei=P3e_UYbTG4iuiAfggoC4Bg&usg=AFQjCNGStDFlon-pIWjSn3gOLdD760PJbw&bvm=bv.47883778,d.aGc"})))))
  )

(deftest graphite-prefix
 (def mk-graphite2 (with-graphite-global {:graphite-host "localhost" :graphite-port 2003} "hoho"))
  (testing "No sub domains"
    (is (= "hoho.com.yahoo"
           (.namespace (mk-graphite2 {:url "http://yahoo.com"}))))
    (is (= "hoho.com.yahoo"
           (.namespace (mk-graphite2 {:url "https://yahoo.com"})))))
  (testing "reverse domain name"
    (is (= "hoho.com.yahoo.www"
           (.namespace (mk-graphite2 {:url "http://www.yahoo.com"})))))
  (testing "reverse domain and combine sub-directories"
    (is (= "hoho.com.stackoverflow.questions-944446-what-is-point-free-style-in-functional-programming"
           (.namespace (mk-graphite2 {:url "http://stackoverflow.com/questions/944446/what-is-point-free-style-in-functional-programming"}))))
    (is (= "hoho.com.github.SupplyFrame-grunf-wiki-graphite-on-ubuntu"
           (.namespace (mk-graphite2 {:url "https://github.com/SupplyFrame/grunf/wiki/graphite-on-ubuntu"})))))

  (testing "long params"
    (is (= "hoho.com.google.www.url#satrc>>>8daGc"
           (.namespace (mk-graphite2 {:url "http://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&ved=0CCwQFjAA&url=http%3A%2F%2Fen.wikipedia.org%2Fwiki%2FFelix&ei=P3e_UYbTG4iuiAfggoC4Bg&usg=AFQjCNGStDFlon-pIWjSn3gOLdD760PJbw&bvm=bv.47883778,d.aGc"})))))
 )







