# grunf

(simple clojure-based http monitoring tool)

## Usage

```
 Switches      Default  Desc
 --------      -------  ----
 -c, --config           Path to the config file 
 -h, --help             Print this message
```

### Example

```
lein run -c conf.example.clj
```

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


## License

Distributed under the Eclipse Public License, the same as Clojure.
