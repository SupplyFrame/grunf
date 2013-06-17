
; grunf | simple http monitoring loop

(ns grunf.bin
  "grunf.main"
  (:use [clojure.tools.cli :only [cli]]
        clj-logging-config.log4j
        grunf.core
        grunf.adapter.log4j
        grunf.adapter.graphite
        grunf.adapter.postal
        grunf.adapter.csv
        grunf.utils)
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
   ["--graphite-prefix" "prefix namespace for graphite"]
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

;; TODOs
;; 4. use (.start (Thread. (fn ...))) instead of future
;;    or pcalls
;; 5. smtp creator in main or util?
;; 6. merge config is super ugly...
;; 7. http options
;; 8. graphite namespace

(defn- create-smtp [options]
  (if-let [smtp-file (:smtp-config options)]
    (try (-> smtp-file
             (slurp)
             (read-string)
             (Mail. (:hostname options) 60000))
         (catch java.io.IOException e
           (throw (java.io.IOException. "smtp config file not found"))))))

(defn- read-urls-config [file]
  "Read config and throw exception if validation failed"
  (try ((comp (fn [config-array]
                 (assert (= (type config-array) clojure.lang.PersistentVector)
                         "Config should be an clojure array")
                 (doseq [config-array-element config-array]
                   (assert (= (type config-array-element) clojure.lang.PersistentArrayMap)
                           "Each element in config array should be a map")
                   (assert (:url config-array-element)
                           "Must have :url in config map"))
                 config-array)
               read-string
               slurp) file)             ; point-free style
       (catch java.io.IOException e
         (throw (java.io.IOException. (str "Config file " file " not found" ))))))


;; TESTED
(defn- daily-logger [file log-pattern]
  (if file                              ; if nil the default prints to console
    (DailyRollingFileAppender.
     (EnhancedPatternLayout. log-pattern)
     file
     "'.'yyyy-MM-dd")))


(defn- with-http-global [{:keys [timeout user-agent]}]
  (fn [http-options-local]
    (merge {:timeout timeout
            :user-agent user-agent}
           http-options-local)))


(defn -main [& argv]
  (let [[options args banner] (apply cli argv cli-options) 
        smtp (create-smtp options)]
    (when (or (:help options) (nil? argv))
      (println default-usage)
      (println banner)
      (System/exit 0))
    (set-loggers!
     "grunf.adapter.log4j"
     {:level (-> options :log-level keyword)
      :out (daily-logger (:log options) log-pattern)
      :pattern log-pattern}
     "grunf.adapter.csv"
     {:out (daily-logger (:csv options) csv-pattern)}
     "grunf.adapter.postal"
     {:pattern log-pattern})
    (try
      (let [smtp-config (create-smtp options)
            urls-config (read-urls-config (:config options))
            csv (if (:csv options) (CSV.))
            log4j (Log4j.)
            mk-graphite (with-graphite-global options (:graphite-prefix options))
            mk-http-option (with-http-global options)]
        (doseq [url-config urls-config]
          (let [adapters (filter identity [log4j smtp-config csv (mk-graphite url-config)])
                http-options (mk-http-option (:http-options url-config))
                task (reduce merge [{:interval (:interval options)}
                                    url-config
                                    http-options])]
            (.start (Thread. (fn [] (fetch task adapters) ))) )
          (Thread/sleep 3000)))
      (catch Exception e
        (println e)
        (System/exit -1)))))
