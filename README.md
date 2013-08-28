# grunf 0.3.8 (Beta)

(simple clojure-based http monitoring tool)

## Install

1. Clone the project to your target machine `git clone https://github.com/SupplyFrame/grunf`

2. Install leiningen if you don't have it on your target machine. you can either
    1. Use the package manager to install it, ex `sudo apt-get install leiningen`
    2. Download [the script](https://raw.github.com/technomancy/leiningen/stable/bin/lein) and put it in your path (ie. `/usr/local/bin`), then `chmod 755 ~/usr/local/bin/lein`.

3. In the grunf source code directory, run `lein deps` to install all the dependency libraries.

4. Checkout [graphite setup guide][] on our wiki!

[graphite setup guide]: https://github.com/SupplyFrame/grunf/wiki/graphite-on-ubuntu


## Usage

read config file and log in console.
```
lein run --log-level info --config conf.example.clj
```

Send events to graphite
```
lein run --config conf.example.clj --graphite-host localhost
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

Forward events to [Riemann][]

[Riemann]: http://riemann.io

```
lein run -c conf.example.clj --riemann-host 127.0.0.1 --riemann-port 5555
``

## Command line options

The command line options for grunf are:

```
Usage:

 Switches               Default    Desc
 --------               -------    ----
 -c, --config                      Path to the config file
 --log                             log path for log4j. If not specified, log to console 
 --log-level            debug      log level for log4j, (fatal|error|warn|info|debug)
 --graphite-host                   Graphite server host
 --graphite-port        2003       Graphite server port
 --graphite-prefix                 prefix namespace for graphite
 --riemann-host                    Riemann host
 --riemann-port         5555
 --hostname             127.0.0.1  This server's hostname
 --csv                             csv log path
 --interval             60000      Default interval for each url request
 --user-agent           Grunf      Default user agent string
 --timeout              6000       Default timeout per request
 -s, --smtp-config                 Path to smtp config file 
 -h, --no-help, --help  false      Print this message
```

## Configuration files format

### URL configurations

The configuration file format for `conf.example.clj` is

```clj
[{:url "http://www.yahoo.com/"
  :interval 1000                      ;; optional
  :validator #(re-find #"yahoo" %)    ;; optional
  :http-options {:timeout 2000        ;; :http-options itself is also optional 
                 :user-agent "Mozilla"}
  :graphite-ns "com.yahoo.www"        ;; defualt to reverse domain name
  :params-fn (map #(hash-map :id %) (iterate inc 0)) ;; programatically control query params
  :riemann-tags ["Yahoo" "Homepage"]
  }
  {:url "http://www.google.com/?search=abc"
   :name "search"                     ;; append to graphite namespace
                                      ;; in this case it would be
                                      ;; "com.google.www.search"
  }      ;; only url is required
]
```

#### Experimental features

`:params-fn` is a function that generate a lazy sequence, and every element in that lazy sequence will be transformed into query params in each query. For example:

```clj
  :params-fn (map #(hash-map :id %) (iterate inc 0)) ;; programatically control query params
```

will transformed into these urls:

```
http://www.google.com/?id=1
http://www.google.com/?id=2
... # and so on
```

And `(cycle [{:search "abc"} {:search "def"}])` will transformed into

```
http://www.google.com/?search=abc
http://www.google.com/?search=def
http://www.google.com/?search=abc
http://www.google.com/?search=def
... # as you expected, a cycle
```

### SMTP configuration

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
HH:MM:ss,SSS,*log type*,*http status*,*url*,*response time (milliseconds)*
```

## Note

This tool is still in experimental status, but all the example configs should work just fine.

### Test covered

* SMTP send mail error and timeout error

* log4j

* csv

* utility functions

## News and changes

* **v0.3.8**
  - Fix graphite exception (should use a thunk)

* **v0.3.7**
  - Fix log4j issues

* **v0.3.6**
  - Added Riemann support

* **v0.3.5**
  - Fixed memory issue by using constant threads

* **v0.3.4**
  - IO exception handling improved in graphite adapter.

* **v0.3.3**
  - Experimental feature `:params-fn`

* **v0.3.2**
  - Wrap (eval validator) to let block, hopefully can solve memory out of space error

* **v0.3.1**
  - Fix graphite CLI option bug

* **v0.3**
  - Automatic graphite namespace generator now covers the full uri path
  - Can use ``--graphite-prefix` to setup prefix namespace for graphite
  - Graphite namespace is now under test coverages
  - Refactored `bin.clj`, many functions go to `utils.clj`

* **v0.2.12**
  - Tests for most adapters (log4j, csv, smtp)

* **v0.2.10**
  - Socket exception handling for graphite

* **v0.2.9**
  - `:name` option in config file

* **v0.2.8**
  - command line options for global request interval, user-agent, and request timeout

## TODOs


1. graphite adapter test

1. Tutorial of writing verification in configuration file.

## License

Distributed under the Eclipse Public License, the same as Clojure.
