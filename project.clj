(defproject grunf "0.3.2"
  :description "simple infrastructure monitoring toolkit"
  :url "https://github.com/SupplyFrame/grunf"
  :main grunf.bin
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [com.draines/postal "1.10.3"]
                 [clj-logging-config "1.9.10"]
                 [log4j/log4j "1.2.16" :exclusions [javax.mail/mail javax.jms/jms com.sun.jdmk/jmxtools com.sun.jmx/jmxri]]
                 [org.clojure/tools.logging "0.2.6"]
                 [http-kit "2.1.2"]
                 [org.clojure/tools.cli "0.2.2"]
                 [org.clojure/clojure "1.5.1"]
                 [clojail "1.0.6"]
                 ])
