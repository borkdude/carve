# Carve

[![Clojars Project](https://img.shields.io/clojars/v/io.github.borkdude/carve.svg)](https://clojars.org/io.github.borkdude/carve)
[![CircleCI](https://circleci.com/gh/borkdude/carve/tree/master.svg?style=shield)](https://circleci.com/gh/borkdude/carve/tree/master)
[![bb compatible](https://raw.githubusercontent.com/babashka/babashka/master/logo/badge.svg)](https://babashka.org)

Carve out the essentials of your Clojure app.

## Rationale

Carve will search through your code for unused vars and will remove them.

## Installation

Add to your `deps.edn` or `bb.edn`:

``` clojure
io.github.borkdude/carve {:git/url "https://github.com/borkdude/carve"
                          :git/sha "<SHA>"}
```

where the latest SHA can be found with:

``` shell
$ git ls-remote https://github.com/borkdude/carve.git refs/heads/master
```

### Babashka script

You can install carve as a babashka script that you can invoke as `carve` with:


``` shell
$ bbin install io.github.borkdude/carve
```

See [bbin](https://github.com/babashka/bbin) for details.

### Clojure tool

To use as a [clojure tool](https://clojure.org/reference/deps_and_cli#tool_install):

``` shell
$ clj -Ttools install io.github.borkdude/carve '{:git/tag "v0.3.5"}' :as carve
```

## How does it work?

Carve invokes [clj-kondo](https://github.com/borkdude/clj-kondo) and uses the
[analysis](https://github.com/borkdude/clj-kondo/tree/master/analysis)
information to check which vars are unused. To remove the relevant bits of code
it uses [rewrite-cljc](https://github.com/lread/rewrite-cljc-playground).

## Usage

The usage for a typical Clojure app looks like:

#### Babashka

To see help:

``` shell
bb -x carve.api/carve! --help
```

To run with options:

``` shell
bb -x carve.api/carve! --paths src test
```

#### Clojure

```
clojure -M:carve --paths src test
```

on the JVM.

#### Clojure tool

As a [clojure tool](https://clojure.org/reference/deps_and_cli#_tool_usage):

``` clojure
$ clj -Tcarve carve! '{:paths ["src"] :report true :report-format :text}'
```

#### Options

Run `carve --help` to see options.

You can also store the config for your project in `.carve/config.edn`. When
invoking carve with no options, the options in `.carve/config.edn` will be used.
When providing options, the CLI options will take precedence over the configuration
in`.carve/config.edn`.

All options:

- `:paths`: a list of paths to analyze. Can be a mix of individual files and directories.
- `:ignore-vars`: a list of vars to ignore. Useful for when the analyzer has it wrong or you just want to keep the var for whatever reason.
- `:api-namespaces`: a list of namespaces of which only unused private vars will
  be reported.
- `:rm-empty-namespaces`: when truthy, also deletes files considered empty, i.e.
  those files in which there is no non-whitespace/non-comment content other than
  the namespace declaration
- `:carve-ignore-file`: a file where ignored vars can be stored, `.carve/ignore`
  by default.
- `:interactive`: ask what to do with an unused var: remove from the file, add
  to `.carve/ignore` or continue. Set to `true` by default.
- `:out-dir`: instead of writing back to the original file, write to this dir.
- `:dry-run`: just print the unused var expression.
- `:aggressive`: runs multiple times until no unused vars are left. Defaults to `false`.
- `:report`: when truthy, prints unused vars to stdout. Implies `:dry-run
  true`. The output format may be set using `:report {:format ...}` where format
  can be `:edn`, `:text` or `:ignore`. The text output can be interpreted by editors like
  Emacs. This option can be combined with `:aggressive`.
- `:silent`: when truthy, does not write to stdout. Implies `:interactive false`.
- `:clj-kondo/config`: a map of clj-kondo config opts that are passed on to
  clj-kondo, which is used to analyze usages. e.g.: passing `{:skip-comments
  true}` will ignore function usage in `(comment)` forms. Note that the config
  in `.clj-kondo/config.edn` is used as well - options passed with this key will
  override options set in the clj-kondo config file.

``` shell
$ clojure -M:carve --paths "test-resources" --dry-run true
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
$ clojure -M:carve --paths "test-resources"
Carving test-resources/app.clj

Found unused var:
(defn unused-function [])

Type Y to remove or i to add app/unused-function to .carve/ignore
n
Found unused var:
(defn another-unused-function [])

Type Y to remove or i to add app/another-unused-function to .carve/ignore
i
...

$ cat .carve/ignore
app/another-unused-function
```

Keep in mind that if you ran `carve` with `{:paths ["src" "test"]}`, there might still be potentially lots of unused code, which wasn't detected simply because there are tests for it.

So after a first cycle of carving you might want to do another run with simply `{:paths ["src"]}`, which will help deleting the rest of the unused code.
*Just beware that this will break all the tests using the code you just deleted, and you'll have to fix/delete them manually.**

Carve also removes any unused refers from namespace `:require` forms. This means
_any_ unused refer, not just refers for functions determined to be unused by
carve.

### CI integration

A good use case for Carve is the CI integration, to ensure that no one can introduce dead code into a codebase.
This example shows how to add this step into CircleCI, but any other CI configuration will be similar.

First add this configuration into a `.circleci/deps.edn` file:

```clojure
{:aliases
 {:carve {:extra-deps {io.github.borkdude/carve {:git/sha "$LATEST_CARVE_SHA"}}
          :main-opts ["-m" "carve.main"]}}}
```

Then configure your build step like this:

```yaml
find_dead_code:
  working_directory: ~/$your-project
  docker:
    - image: circleci/clojure:openjdk-11-tools-deps

  steps:
    - checkout
    - run: mkdir -p ~/.clojure && cp .circleci/deps.edn ~/.clojure/deps.edn
    - run: clojure -M:carve --opts '{:paths ["src" "test"] :report {:format :text}}'
```

If the `report` step finds any dead code it exits with status code `1`, thus failing the build step.

### Emacs

#### carve.el

A simple emacs integration is provided by [carve.el](https://github.com/oliyh/carve.el).

It lets you run carve with a simple key binding and opens a result buffer where you can navigate to the results and add them to your ignore file with a single keystroke.

#### Report mode

Running carve with in report mode (for example `clojure -M:carve --paths
src test --report true --report-format :text}'`) you can make all the links clickable
by switching to compilation-mode.

<img src="assets/eshell.png">

Using `:report-format :ignore` returns the list of unused vars in the same format as .carve/ignore so you can create the initial ignore file or append to an existing one.
For example with:

    carve --paths "src" "test" --report true --report-format :ignore' > .carve/ignore

## Articles

- [Carve that Clojure codebase](https://juxt.pro/blog/carve) by Andrea Crotti

## Dev


### Running tests

If you want to run tests in Emacs and Cider you need to use the test alias, or
it will fail while trying to load the `test.check` library.  You can place this in
your `.dir-locals.el` file in the root directory to always use the test alias:

```elisp
((clojure-mode . ((cider-clojure-cli-global-options . "-R:test"))))
```

or alter the command used by `cider-jack-in` by prefixing the invocation with
`C-u`.

## License

Copyright © 2019-2023 Michiel Borkent

Distributed under the EPL License. See LICENSE.
