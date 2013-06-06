
; grunf | simple http monitoring loop

(ns grunf.bin
  "grunf.main"
  (:require [org.httpkit.client :as http]
            [grunf.core :as grunf])
  (:use [clojure.tools.cli :only [cli]]
        clj-logging-config.log4j)
  (:import [org.apache.log4j DailyRollingFileAppender EnhancedPatternLayout])
  (:gen-class))


(defn -main [& argv]
  (let [[options args banner]
        (cli argv
             ["-c" "--config" "Path to the config file"]
             ["-h" "--help" "Print this message"])]
    (when (or (:help options) (nil? argv))
      (println banner)
      (System/exit 0))
    (set-loggers! "grunf.core"
                  {:level :debug
                   :out (DailyRollingFileAppender.
                         (EnhancedPatternLayout. "%d{ISO8601}{GMT} [%-5p] [%t] - %m%n")
                         "logs/foo.log"
                         "'.'yyyy-MM-dd")})
    (->> (slurp (:config options))
         (read-string)
         (pmap grunf/fetch))))


;; (defn -main
;;   "Start Grunf. Pass hostnames/poll interval as (first argv)"
;;   [& argv]
;;   (let [[options args banner]
;;         (cli argv
;;              ["-c" "--config" "path to the config file"]
;;              ["-s" "--hosts" "list of host names spearated by commas" :parse-fn #(vec (.split % ","))]
;;              ["-t" "--rps" "requests per second" :parse-fn #(Integer. %)]
;;              ["--help" "print this help message" :default false :flag true])]
;;     (when (or (:help options) (nil? argv))
;;       (println banner)
;;       (System/exit 0))
;;     (try
;;       (qs/initialize)
;;       (qs/start)
;;       (let [[hosts interval]
;;             ((juxt :hosts :rps)
;;              (if-let [config-file (:config options)]
;;                (->> (slurp config-file)  ; TODO: No error handling when file not found
;;                     (read-string))       ; TODO: No error handling when parse error
;;                options))]
;;         (when (or (nil? hosts) (nil? interval))
;;           (println "must specify config file or '--hosts http://google.com,http://yahoo.com --interval 1000'")
;;           (System/exit 0))
;;         ;; (map #(submitFetchJob % interval) hosts)
;;         (lets-puts-some-stress (first hosts) interval)
;;         )
;;       (catch Exception e
;;         (println e)
;;         (error e "Error starting the app")))))
