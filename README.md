# grunf 0.2.7 (Alpha)

(simple clojure-based http monitoring tool)

## Usage

read config file and log in concole
```
lein run --log-level info --config conf.example.clj
```

Can also read config from stdin
```
lein run < conf.example.clj
```

Log in file `logs/foo.log`. Note `lein trampoline` let you send lein process to background without pausing the program.

```
mkdir logs
lein trampoline run --log logs/foo.log -c conf.example.clj &
tail -f logs/foo.log
```

Read smtp config, will send mails when receive error
```
lein run -c conf.example.clj -s smtp.example.clj
```

export csv in logs/bar.csv
```
lein run -c conf.example.clj --csv logs/bar.csv
```

## Command line options

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
 --csv                             csv log path
 -s, --smtp-config                 Path to smtp config file 
 -h, --no-help, --help  false    Print this message
```

## Configuration files format

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

For full documentaion of SMTP setup, please visit [postal -- internet email library for clojure](https://github.com/drewr/postal).

## Output format

### CSV

The CSV fields are

```
HH:MM:ss,SSS, *log type*, *http status*, *url*, *response time (milliseconds)*
```

## Note

This tool is still in experimental status, but all the example configs should work just fine.

## TODO

1. Write tutorial of using grunf with graphite

2. Refactor to make it testable (and more funtional idomatic).

3. More options for global config

## License

Distributed under the Eclipse Public License, the same as Clojure.
