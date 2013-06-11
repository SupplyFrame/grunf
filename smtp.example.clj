;; Check the API on https://github.com/drewr/postal

^{:host "smtp.gmail.com"
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
