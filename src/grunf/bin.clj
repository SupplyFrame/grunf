
; grunf | simple http monitoring loop

(ns grunf.bin
  "grunf.main"
  (:use [clojure.tools.cli :only [cli]]
        clj-logging-config.log4j
        grunf.core
        grunf.adapter.log4j
        grunf.adapter.graphite
        grunf.adapter.postal
        grunf.adapter.csv)
  (:import [org.apache.log4j DailyRollingFileAppender EnhancedPatternLayout]
           [grunf.adapter.log4j Log4j]
           [grunf.adapter.graphite Graphite]
           [grunf.adapter.postal Mail]
           [grunf.adapter.csv CSV])
  (:gen-class))


(def log-pattern "Log4j pattern layout"
  "%d{ISO8601}{GMT} [%-5p] [%t] - %m%n")

(def csv-pattern "%d{ABSOLUTE},[%-5p],%m%n")

(def cli-options
  "Grunf command line options"
  [["-c" "--config" "Path to the config file"]
   ["--log" "log path for log4j. If not specified, log to console"]
   ["--log-level" "log level for log4j, (fatal|error|warn|info|debug)" :default "debug"]
   ["--graphite-host" "Graphite server host"]
   ["--graphite-port" "Graphite server port" :default 2003 :parse-fn #(Integer. %)]
   ["--hostname" "This server's hostname" :default "127.0.0.1"]
   ["--csv" "csv log path"]
   ["--interval" "Default interval for each url request" :default 60000 :parse-fn #(Integer. %)]
   ["--user-agent" "Default user agent string" :default "Grunf"]
   ["--timeout" "Default timeout per request" :default 6000 :parse-fn #(Integer. %)]
   ["-s" "--smtp-config" "Path to smtp config file"]
   ["-h" "--[no-]help" "Print this message" :default false]])

(def default-usage
  "Example:
lein run --log-level info --config conf.example.clj
lein run --config conf.example.clj --graphite-host localhost # Send events to graphite
lein trampoline run --log logs/foo.log -c conf.example.clj & tail -f logs/foo.log
# SMTP example:
lein run -c conf.example.clj -s smtp.example.clj
# CSV output example
lein run -c conf.example.clj --csv logs/bar.csv")

(defn- verify-config [config-array]
  "Verify grunf config file using assertion and exception handling"
  (assert (= (type config-array) clojure.lang.PersistentVector)
          "Config should be an clojure array")
  (doseq [config-array-element config-array]
    (assert (= (type config-array-element) clojure.lang.PersistentArrayMap)
            "Each element in config array should be a map")
    (assert (:url config-array-element)
            "Must have :url in config map"))
  config-array)

(defn- url->rev-host
  "Resove host, than reverse the order
   http://www.yahoo.com/search.html -> com.yahoo.www"
  [url]
  (->> url
       (re-find #"(?<=://).+?(?=/|$)")
       (.split #"\.")
       (reverse)
       (clojure.string/join ".")))

(defn- create-log4j [options]
  (Log4j. "grunf.adapter.log4j"
          (-> options :log-level keyword) log-pattern
          (if (:log options)
            (DailyRollingFileAppender.
             (EnhancedPatternLayout. log-pattern)
             (:log options)
             "'.'yyyy-MM-dd")
            :console)))

(defn- create-graphite [options]
  )

(defn- create-smtp [options]
  (if-let [smtp-file (:smtp-config options)]
    (try (-> smtp-file
             (slurp)
             (read-string)
             (Mail. (:hostname options)))
         (catch java.io.IOException e
           (println "smtp config file not found")
           (System/exit -1))
         (catch Exception e
           (println "read smtp-config file failed")
           (System/exit -1)))))

(defn -main [& argv]
  (let [[options args banner] (apply cli argv cli-options)
        log4j (create-log4j options)
        smtp (create-smtp options)]
    (when (:help options)
      (println default-usage)
      (println banner)
      (System/exit 0))
    (init-logger log4j)
    (set-loggers! "grunf.adapter.postal" ;; quick hack
                  {:pattern log-pattern})
    (when (:csv options)
      (set-loggers! "grunf.adapter.csv"
                    {:out
                     (DailyRollingFileAppender.
                      (EnhancedPatternLayout. csv-pattern)
                      (:csv options)
                      "'.'yyyy-MM-dd")}))
    (map
     (fn [{url :url
           graphite-ns :graphite-ns
           interval :interval
           name :name
           http-options :http-options :as task}
          ]
       (let [graphite-ns (cond graphite-ns graphite-ns
                               name (str (url->rev-host url) "." name)
                               :else (url->rev-host url))
             graphite (if (:graphite-host options)
                        (Graphite. graphite-ns
                                   (:graphite-host options)
                                   (:graphite-port options)))
             default-http-options {:timeout (:timeout options)
                                   :user-agent (:user-agent options)}
             merge-default (reduce
                            merge
                            [{:interval (:interval options)}
                             task
                             {:http-options (merge default-http-options http-options)}])]
         (Thread/sleep 1000) ;; pollite request
         (future
           (fetch merge-default
                  (filter identity [log4j smtp (if (:csv options) (CSV.)) graphite])))))
     (try
       (->> (or (:config options) *in*)
            (slurp)
            (read-string)
            (verify-config))
       (catch java.io.IOException e
         (println "Config file not found:" (:config options))
         (System/exit -1))
       (catch AssertionError e ;; clojure assertion
         (println "config file error:" e)
         (System/exit -1))
       (catch Exception e
         (println "Config file error" e)
         (System/exit -1))))))
