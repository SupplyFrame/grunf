(ns grunf.utils
  (:require [ clojure.string :as s]))

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
      (s/join (concat
               (take 5 word-chars)
               ">>>"
               (take-last 5 word-chars))))
    nil))

(defn- url-sub
  [url]
  (s/join "-"
          (map #(s/replace % #"\." "_")
               (re-seq #"(?<=/)\w+(?=/)|(?<!//)(?<=/)[\-\w.]+(?=\?|$)" url))))

(defn url->rev-full
  [url]
  (let [rev-host (url->rev-host url)
        sub-dir (url-sub url)
        params (url-tail url)]
    (if (empty? sub-dir)
      rev-host
      (s/join
       (concat rev-host "." sub-dir
               (if params
                 (concat "#" params)))))))

(defn url->rev-host
  "Resove host, than reverse the order
   http://www.yahoo.com/search.html -> com.yahoo.www"
  [url]
  (->> url
       (re-find #"(?<=://).+?(?=/|$)")
       (.split #"\.")
       (reverse)
       (clojure.string/join ".")))

(defn verify-config 
  "Verify grunf config file using assertion and exception handling"
  [config-array]
  (assert (= (type config-array) clojure.lang.PersistentVector)
          "Config should be an clojure array")
  (doseq [config-array-element config-array]
    (assert (= (type config-array-element) clojure.lang.PersistentArrayMap)
            "Each element in config array should be a map")
    (assert (:url config-array-element)
            "Must have :url in config map"))
  config-array)


