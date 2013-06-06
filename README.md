# grunf

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
```

The command line options for grunf are:

```
Usage:

 Switches               Default  Desc
 --------               -------  ----
 -c, --config                    Path to the config file
 --log                           log path for log4j. If not specified, log to console 
 --log-level            debug    log level for log4j, (fatal|error|warn|info|debug)
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

## TODO

1. Write tutorial of using grunf with graphite

2. Refactor to make it testable (and more funtional idomatic).

## License

Distributed under the Eclipse Public License, the same as Clojure.
