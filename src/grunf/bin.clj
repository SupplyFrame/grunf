
; grunf | simple http monitoring loop

(ns grunf.bin
	"grunf.main"
	(:require [clj-http.client :as client]	
		  [clojurewerkz.quartzite.scheduler :as qs]
		  [clojurewerkz.quartzite.triggers :as t]
		  [clojurewerkz.quartzite.jobs :as j]
		  [clojurewerkz.quartzite.conversion :as qc])
	(:use [clojurewerkz.quartzite.jobs :only [defjob]]
	      [clojurewerkz.quartzite.schedule.simple :only [schedule with-repeat-count with-interval-in-milliseconds repeat-forever]])
	(:gen-class))

(defn fetch
	"fetch given url"
	[url]
	(count (:body (client/get url))))

(defn fetchTime
	"get url fetch time"
	[url]
	(map #(Double/parseDouble %) (re-seq #"[0-9]+.[0-9]+" (with-out-str (time (fetch url))))))

(defjob FetchJob
	[ctx]
	(let [m (qc/from-job-data ctx)]
		(println (conj (fetchTime (get m "url")) (get m "url") (System/currentTimeMillis)))))

(defn submitFetchJob
	"submit fetch job for execution"
	[url interval]
	(let [job (j/build
		  (j/of-type FetchJob)
		  (j/using-job-data {"url" url})
		  (j/with-identity (j/key (str "jobs.fetch." url) )))
		  trigger (t/build
			  (t/with-identity (t/key (str "triggers." url) ))
			  (t/start-now)
			  (t/with-schedule (schedule
			  			(repeat-forever)
			  			(with-interval-in-milliseconds interval))))]
		  (qs/schedule job trigger)))

(defn error
        [exception message]
        (println message))

(defn -main
	"Start Grunf. Pass hostnames/poll interval as (first argv)"
	[& argv]
	(if (< (count argv) 1)
		(do
			(println "usage: lein run -m grunf.bin '([hostname_list] poll_interval)'")
			(System/exit 0)))
	(try
		(qs/initialize)
		(qs/start)
		(doseq [url (first (read-string (first argv)))] (submitFetchJob url (last (read-string (first argv)))))
		(catch Exception e
			(println e)
			(error e "Error starting the app"))))
