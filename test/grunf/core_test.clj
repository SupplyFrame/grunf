(ns grunf.core-test
  (:require grunf.bin)
  (:use clojure.test
        grunf.core ;; we need protocol definition here
        grunf.adapter.log4j
        grunf.adapter.csv
        grunf.adapter.postal
        clj-logging-config.log4j)
  (:import [grunf.adapter.log4j Log4j]
           [grunf.adapter.csv CSV]
           [grunf.adapter.postal Mail]))

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
  (def log4j (Log4j.))
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

(deftest test-csv
  (def csv (CSV.))
  (def context {:error "error"
                :status 200
                :headers {:location "http://test2.org"}
                :opts {:validator (constantly true)
                       :validator-source '(constantly true)
                       :url "http://test.org"
                       :start (System/currentTimeMillis)
                       }})

  (testing "log-success"
    (expect #"\d\d:\d\d:\d\d,\d+,\[INFO \],200,http://test.org,\d+"
            (set-logger! "grunf.adapter.csv"
                         :pattern grunf.bin/csv-pattern)
            ((log-success csv) context)))

  (testing "log-validate-error"
    (expect #"\d\d:\d\d:\d\d,\d+,\[ERROR\],200,http://test.org,\d+"
            (set-logger! "grunf.adapter.csv"
                         :pattern grunf.bin/csv-pattern)
            ((log-validate-error csv) context)))

  (testing "log-redirect"
    (expect #"^$"
            (set-logger! "grunf.adapter.csv"
                         :pattern grunf.bin/csv-pattern)
            ((log-redirect csv) context)))

  (testing "log-unknown-error"
    (expect #"\d\d:\d\d:\d\d,\d+,\[ERROR\],200,http://test.org,\d+"
            (set-logger! "grunf.adapter.csv"
                         :pattern grunf.bin/csv-pattern)
            ((log-unknown-error csv) context)))

  (testing "log-client-error"
    (expect #"\d\d:\d\d:\d\d,\d+,\[ERROR\],200,http://test.org,\d+"
            (set-logger! "grunf.adapter.csv"
                         :pattern grunf.bin/csv-pattern)
            ((log-client-error csv) context)))

  (testing "log-server-error"
    (expect #"\d\d:\d\d:\d\d,\d+,\[ERROR\],200,http://test.org,\d+"
            (set-logger! "grunf.adapter.csv"
                         :pattern grunf.bin/csv-pattern)
            ((log-server-error csv) context)))
  )

(deftest test-smtp
  (def context {:error "error"
                :status 200
                :headers {:location "http://test2.org"}
                :opts {:validator (constantly true)
                       :validator-source '(constantly true)
                       :url "http://test.org"
                       :start (System/currentTimeMillis)
                       }})
  (testing "mail error in log-unknown-error"
    (expect #"\d{4}-\d\d-\d\d [\d:,]+ \[ERROR\] \[.*\]"
            (set-logger! "grunf.adapter.postal"
                         :pattern grunf.bin/log-pattern)
            ((log-unknown-error (Mail. {:from "abc"} "abc.def" 60)) context)
            (Thread/sleep 160)
            ))

  (testing "mail error in log-server-error"
    (expect #"\d{4}-\d\d-\d\d [\d:,]+ \[ERROR\] \[.*\]"
            (set-logger! "grunf.adapter.postal"
                         :pattern grunf.bin/log-pattern)
            ((log-server-error (Mail. {:from "abc"} "abc.def" 60)) context)
            (Thread/sleep 160)
            ))

  (testing "mail error in log-client-error"
    (expect #"\d{4}-\d\d-\d\d [\d:,]+ \[ERROR\] \[.*\]"
            (set-logger! "grunf.adapter.postal"
                         :pattern grunf.bin/log-pattern)
            ((log-client-error (Mail. {:from "abc"} "abc.def" 60)) context)
            (Thread/sleep 160)
            ))

  (testing "mail error in log-validate-error"
    (expect #"\d{4}-\d\d-\d\d [\d:,]+ \[ERROR\] \[.*\]"
            (set-logger! "grunf.adapter.postal"
                         :pattern grunf.bin/log-pattern)
            ((log-validate-error (Mail. {:from "abc"} "abc.def" 60)) context)
            (Thread/sleep 160)
            ))
  
  (def timeout-mail (Mail. ^{:host "smtp.gmail.com"
                             :user "example@gmail.com"
                             :pass "password"
                             :port 1234
                             :tls true
                             }
                           {:from "sysalerts@example.com"
                            :to ["user1@gmail.com" "user2@gmail.com"]
                            :subject "Will be overwirtten by grunf!"
                            :body "Will be overwritten by grunf!"
                            }
                           "example.com" 600))
  (testing "timeout in log-unknown-error"
    (expect #"\d{4}-\d\d-\d\d [\d:,]+ \[ERROR\] \[.*\] - send mail timeout: \d+ #<TimeoutException .*>"
            (set-logger! "grunf.adapter.postal"
                         :pattern grunf.bin/log-pattern)
            ((log-unknown-error timeout-mail) context)
            (Thread/sleep 1200)))

  (testing "timeout in log-client-error"
    (expect #"\d{4}-\d\d-\d\d [\d:,]+ \[ERROR\] \[.*\] - send mail timeout: \d+ #<TimeoutException .*>"
            (set-logger! "grunf.adapter.postal"
                         :pattern grunf.bin/log-pattern)
            ((log-client-error timeout-mail) context)
            (Thread/sleep 1200)))

  (testing "timeout in log-server-error"
    (expect #"\d{4}-\d\d-\d\d [\d:,]+ \[ERROR\] \[.*\] - send mail timeout: \d+ #<TimeoutException .*>"
            (set-logger! "grunf.adapter.postal"
                         :pattern grunf.bin/log-pattern)
            ((log-server-error timeout-mail) context)
            (Thread/sleep 1200)))

  (testing "timeout in log-validate-error"
    (expect #"\d{4}-\d\d-\d\d [\d:,]+ \[ERROR\] \[.*\] - send mail timeout: \d+ #<TimeoutException .*>"
            (set-logger! "grunf.adapter.postal"
                         :pattern grunf.bin/log-pattern)
            ((log-validate-error timeout-mail) context)
            (Thread/sleep 1200)))
  )
