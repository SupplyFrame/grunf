# grunf

(simple clojure-based http monitoring tool)

## Usage

Type these commands:

```
mkdir logs
lein trampoline run -c conf.example.clj &
tail -f logs/foo.log
```

The command line options for grunf are:

```
 Switches      Default  Desc
 --------      -------  ----
 -c, --config           Path to the config file 
 -h, --help             Print this message
```

The configuration file format for `conf.example.clj` is

```clj
[{:name "yahoo"
  :url "http://www.yahoo.com/"
  :meta {:from "0.0.0.0"}
  :interval 1000
  :validator #(re-find #"yahoo" %)
  :http-options {:timeout 2000
                 :user-agent "Mozilla"}
  :redirect True
  }]
```

## TODO

Write tutorial of using grunf with graphite

## License

Distributed under the Eclipse Public License, the same as Clojure.
