#!/usr/bin/env bb

(require '[babashka.pods :as pods])

(pods/load-pod 'clj-kondo/clj-kondo "2021.10.19")
(require '[pod.borkdude.clj-kondo :as clj-kondo])
;; define clj-kondo.core ns which is used by carve
(intern (create-ns 'clj-kondo.core) 'run! clj-kondo/run!)

(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {borkdude/carve ;; {:local/root "."}
                        {:git/url "https://github.com/borkdude/carve"
                         :git/sha "dd912e1c18abb99696cd118d69473232ec140aeb"}
                        borkdude/spartan.spec {:git/url "https://github.com/borkdude/spartan.spec"
                                               :sha "12947185b4f8b8ff8ee3bc0f19c98dbde54d4c90"}}})

(require '[spartan.spec]) ;; defines clojure.spec

(with-out-str ;; silence warnings about spartan.spec + with-gen
  (binding [*err* *out*]
    (require '[carve.api :as carve])))

;; again to make clj-kondo happy
(require '[carve.main])
(apply carve.main/-main *command-line-args*)
