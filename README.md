# grunf

(simple clojure-based http monitoring tool)

## Usage

lein run -m grunf.bin '([hostname_list] poll_interval(ms))'

Example:

lein run -m grunf.bin '(["http://www.google.com" "http://www.yahoo.com" "http://www.bing.com"] 1000)'

## License

Distributed under the Eclipse Public License, the same as Clojure.
