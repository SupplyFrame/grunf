# grunf 0.2.6 (Alpha)

(simple clojure-based http monitoring tool)

## Usage

Type these commands:

```
lein run --log-level info --config conf.example.clj
# or
lein run < conf.example.clj # Can also read config from stdin
# or
mkdir logs
lein trampoline run --log logs/foo.log -c conf.example.clj &
tail -f logs/foo.log
#or
lein run -c conf.example.clj -s smtp.example.clj
```

The command line options for grunf are:

```
Usage:

 Switches               Default  Desc
 --------               -------  ----
 -c, --config                    Path to the config file
 --log                           log path for log4j. If not specified, log to console 
 --log-level            debug    log level for log4j, (fatal|error|warn|info|debug)
 --graphite-host                 Graphite server host
 --graphite-port        2003     Graphite server port
 --hostname             127.0.0.1  This server's hostname
 -s, --smtp-config                 Path to smtp config file 
 -h, --no-help, --help  false    Print this message
```

The configuration file format for `conf.example.clj` is

```clj
[{:url "http://www.yahoo.com/"
  :interval 1000                      ;; optional
  :validator #(re-find #"yahoo" %)    ;; optional
  :http-options {:timeout 2000        ;; :http-options itself is also optional 
                 :user-agent "Mozilla"}
  :graphite-ns "com.yahoo.www"        ;; defualt to reverse domain name
  }
  {:url "http://www.google.com"}      ;; only url is required
]
```

The smtp configuration file for `smtp.example.clj` is

```clj
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
```

## Note

This tool is still in experimental status, but all the example configs should work just fine.

## TODO

1. Write tutorial of using grunf with graphite

2. Refactor to make it testable (and more funtional idomatic).

3. Handle smtp error and print error messages

4. More options for global config

## License

Distributed under the Eclipse Public License, the same as Clojure.
