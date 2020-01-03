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
                                     :sha "81b3538ce0e166b2706b2f976e933a690869180d"}}
        :main-opts ["-m" "carve.main"]}
```

or use any later SHA.

## How does it work?

Carve invokes [clj-kondo](https://github.com/borkdude/clj-kondo) and uses the [analysis](https://github.com/borkdude/clj-kondo/tree/master/analysis) information to check which vars are unused. To remove the relevant bits of code it uses [rewrite-cljc](https://github.com/lread/rewrite-cljc-playground).

## Usage

The usage for a typical Clojure app looks like:

```
clojure -A:carve --opts '{:paths ["src" "test"]}'
```

Currently `carve` only has one command line option, `--opts`, which
expects an EDN map of the following options of which only `:paths` is required:

- `:paths`: a list of paths to analyze. Can be a mix of individual files and directories.
- `:ignore-vars`: a list of vars to ignore. Useful for when the analyzer has it wrong or you just want to keep the var for whatever reason.
- `:api-namespaces`: a list of namespaces of which only unused private vars will
  be reported.
- `:carve-ignore-file`: a file where ignored vars can be stored, `.carve_ignore`
  by default.
- `:interactive?`: ask what to do with an unused var: remove from the file, add
  to `.carve_ignore` or continue. Set to `true` by default.
- `:out-dir`: instead of writing back to the original file, write to this dir.
- `:dry-run?`: just print the unused var expression.
- `:aggressive?`: runs `carve` multiple times until no unused vars are left.
- `:report`: when truthy, prints unused vars to stdout. Implies `:dry-dun?
  true`. The output format may be set using `:report {:format ...}` where format
  can be `:edn` or `:text`. The text output can be interpreted by editors like
  Emacs. This option can be combined with `:aggressive?`.

``` shell
$ clojure -A:carve --opts '{:paths ["test-resources"] :dry-run? true}'
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
$ clojure -A:carve --opts '{:paths ["test-resources"]}'
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
