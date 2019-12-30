# Carve

Carve out the essentials of your Clojure app.

## Rationale

Carve will search through your code for unused vars and will remove them.

## Status

Experimental. Use with caution! Breaking changes may happen. Feedback and bugfix PRs welcome.

## Installation

Add to your `deps.edn` under the `:aliases` key:

``` clojure
:carve {:extra-deps {borkdude/carve {:git/url "https://github.com/borkdude/carve"
                                     :sha "284786098ef363dc52a49755687e9eb87ca41f59"}}
        :main-opts ["-m" "carve.main"]}
```

or use any later SHA.

## Usage

Currently `carve` only has one command line option, `-o` or `--opts` which
expects an EDN map of the following options of which only `:paths:` is required:

- `:paths`: a list of paths to analyze
- `:ignore-vars`: a list of vars to ignore
- `:ignore-namespaces`: a list of namespaces to ignore. Note: private vars are
  still considered to be removed.
- `:carve-ignore-file`: a file where ignored vars can be stored, `.carve_ignore`
  by default.
- `:interactive?`: ask what to do with an unused var: remove from the file, add
  to `.carve_ignore` or continue. Set to `true` by default.
- `:out-dir`: instead of writing back to the original file, write to this dir.
- `:dry-run?`: just print the unused var expression.

``` shell
$ clojure -A:carve -o '{:paths ["test-resources"] :dry-run? true}'
Carving test-resources/app.clj

Found unused var:
(defn unused-function [])

...

Carving test-resources/api.clj

Found unused var:
(defn- private-lib-function [])

...
```

``` shell
$ clojure -A:carve -o '{:paths ["test-resources"]}'
Carving test-resources/app.clj

Found unused var:
(defn unused-function [])

Type Y to remove or i to add app/unused-function to .carve_ignore
n
Found unused var:
(defn another-unused-function [])

Type Y to remove or i to add app/another-unused-function to .carve_ignore
i
Found unused var:
(defn -main []
  (used-function))

Type Y to remove or i to add app/another-unused-function to .carve_ignore
i
...

$ cat .carve_ignore
app/another-unused-function
app/-main
```

## License

Copyright © 2019 Michiel Borkent

Distributed under the EPL License. See LICENSE.
