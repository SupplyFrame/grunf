# grunf

(simple clojure-based http monitoring tool)

## Usage

```
 Switches           Default  Desc
 --------           -------  ----
 -c, --config                path to the config file
 -s, --hosts                 list of host names spearated by commas
 -t, --rps                   requests per second
 --no-help, --help  false    print this help message
```

Example

```
lein run -- -s http://google.com,http://yahoo.com -t 3000
lein run -- -c conf.example.com
```

## License

Distributed under the Eclipse Public License, the same as Clojure.
