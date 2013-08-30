(ns grunf.utils
  (:require [ clojure.string :as s])
  (:use grunf.adapter.graphite)
  (:import [grunf.adapter.graphite Graphite]
))



(defn url->rev-host
  "Resove host, than reverse the order
   http://www.yahoo.com/search.html -> com.yahoo.www"
  [url]
  (->> url
       (re-find #"(?<=://).+?(?=/|$)")
       (.split #"\.")
       (reverse)
       (clojure.string/join ".")))

(defn- url-tail
  [url]
  (if-let [tails (re-find #"(?<=\?).*$" url)]
    (let [word-chars (re-seq #"\w" tails)]
      (if (> (count word-chars) 10)
        (clojure.string/join (concat
                              (take 5 word-chars)
                              ">>>"
                              (take-last 5 word-chars)))
        (clojure.string/join word-chars)))
    nil))

(defn- url-sub
  [url]
  (clojure.string/join
   "-" (map #(clojure.string/replace % #"\." "_")
            (re-seq #"(?<=/)\w+(?=/)|(?<!//)(?<=/)[\-\w.]+(?=\?|$)" url))))

(defn url->rev-full
  [url]
  (let [rev-host (url->rev-host url)
        sub-dir (url-sub url)
        params (url-tail url)]
    (if (empty? sub-dir)
      rev-host
      (clojure.string/join
       (concat rev-host "." sub-dir
               (if params
                 (concat "#" params)))))))


(defn with-graphite-global
  ([options] (with-graphite-global options nil))
  ([{:keys [graphite-host graphite-port]} prefix-ns]
     (if graphite-host
       (fn [{:keys [graphite-ns name url]}]
         (let [prefix-ns (if prefix-ns (str prefix-ns "."))
               ns (str prefix-ns
                       (cond graphite-ns graphite-ns
                             name (str (url->rev-host url) "." name)
                             :else (url->rev-full url)))]
           (Graphite. ns graphite-host graphite-port)))
       (fn [_]))))
