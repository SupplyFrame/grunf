(ns grunf.core-test
  (:require grunf.bin)
  (:use clojure.test
        grunf.core
        grunf.adapter.log4j
        clj-logging-config.log4j)
  (:import [grunf.adapter.log4j Log4j]))

;; copied from clj-logging-config
;; written by Malcolm Sparks.
(defmacro capture-stdout [& body]
  `(let [out# System/out
         baos# (java.io.ByteArrayOutputStream.)
         tempout# (java.io.PrintStream. baos#)]
     (try
       (System/setOut tempout#)
       ~@body
       (String. (.toByteArray baos#))
       (finally
         (System/setOut out#)))))

(defmacro expect [regex & body]
  `(let [out# (capture-stdout (do ~@body))]
     (is (re-find ~regex out#))))

(use-fixtures :each (fn [f] (reset-logging!) (f)))

;; work on log4j/csv tests first

(deftest test-log4j
  (def log4j (Log4j. nil nil nil nil))
  (def context {:error "error"
                :status 200
                :headers {:location "http://test2.org"}
                :opts {:validator (constantly true)
                       :validator-source '(constantly true)
                       :url "http://test.org"
                       :start (System/currentTimeMillis)
                       }})

  (testing "log-success"
    (expect #"\d{4}-\d\d-\d\d [\d:,]+ \[INFO \] \[.+\] - 200 http://test.org response time \(msec\): \d+"
            (set-logger! "grunf.adapter.log4j"
                 :pattern grunf.bin/log-pattern)
            ((log-success log4j) context)))
  
  (testing "log-validate-error" 
    (expect #"\d{4}-\d\d-\d\d [\d:,]+ \[ERROR\] \[.*\] - 200 http://test.org response time \(msec\): \d+ , -- validate failed -- validator: \(.+\)"
            (set-logger! "grunf.adapter.log4j"
                         :pattern grunf.bin/log-pattern)
            ((log-validate-error log4j) context)))

  (testing "log-redirect"
    (expect #"\d{4}-\d\d-\d\d [\d:,]+ \[INFO \] \[.*\] - 200 redirect http://test.org -> http://test2.org"
            (set-logger! "grunf.adapter.log4j"
                         :pattern grunf.bin/log-pattern)
            ((log-redirect log4j) context)))
  
  (testing "log-unknown-error"
    (expect #"\d{4}-\d\d-\d\d [\d:,]+ \[ERROR\] \[.*\] - 200 http://test.org error \{.*:location http://test2.org.*\}"
            (set-logger! "grunf.adapter.log4j"
                         :pattern grunf.bin/log-pattern)
            ((log-unknown-error log4j) context)))

  (testing "log-client-error"
    (expect #"\d{4}-\d\d-\d\d [\d:,]+ \[ERROR\] \[.*\] - 200 http://test.org error \{.*:location http://test2.org.*\}"
            (set-logger! "grunf.adapter.log4j"
                         :pattern grunf.bin/log-pattern)
            ((log-client-error log4j) context)))

  (testing "log-server-error"
    (expect #"\d{4}-\d\d-\d\d [\d:,]+ \[ERROR\] \[.*\] - 200 http://test.org error \{.*:location http://test2.org.*\}"
            (set-logger! "grunf.adapter.log4j"
                         :pattern grunf.bin/log-pattern)
            ((log-server-error log4j) context)))
    )
