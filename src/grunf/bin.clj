
; grunf | simple http monitoring loop

(ns grunf.bin
  "grunf.main"
  (:require [org.httpkit.client :as http]
            [grunf.core :as grunf])
  (:use [clojure.tools.cli :only [cli]]
        clj-logging-config.log4j)
  (:import [org.apache.log4j DailyRollingFileAppender EnhancedPatternLayout])
  (:gen-class))


(def log-pattern "%d{ISO8601}{GMT} [%-5p] [%t] - %m%n")

(defn -main [& argv]
  (let [[options args banner]
        (cli argv
             ["-c" "--config" "Path to the config file"]
             ["-h" "--[no-]help" "Print this message" :default false]
             ["--log" "log path for log4j. If not specified, log to console"]
             ["--log-level" "log level for log4j, (fatal|error|warn|info|debug)" :default "debug"])]
    (when (or (:help options) (nil? argv))
      (println banner)
      (System/exit 0))
    (set-loggers! "grunf.core"
                  {:level (-> options :log-level keyword)
                   :pattern log-pattern
                   :out (if (:log options)                          
                          (DailyRollingFileAppender. (EnhancedPatternLayout. log-pattern)
                                                     (:log options)
                                                     "'.'yyyy-MM-dd")
                          :console)})
    (->> (try
           (slurp (:config options))
           (catch java.io.IOException e
             (println "File not found:" (:config options))
             (System/exit -1)))
         (read-string) ;; should handle parse error exception
         ;; should varify the parsed string
         (pmap grunf/fetch))))
