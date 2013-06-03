(defproject grunf "0.1.0-SNAPSHOT"
  :description "simple infrastructure monitoring toolkit"
  :url "https://github.com/SupplyFrame/grunf"
  :main grunf.bin
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [http-kit "2.1.2"]
                 [clj-time "0.5.1"]
                 [com.taoensso/timbre "1.6.0"]
                 [org.clojure/tools.cli "0.2.2"]
                 [org.clojure/clojure "1.5.1"]
                 [org.slf4j/slf4j-nop "1.7.2"]
                 [clj-http "0.4.1"]
                 [clojurewerkz/quartzite "1.0.1"]])
