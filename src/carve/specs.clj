(ns carve.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::paths (s/coll-of string?))
(s/def ::ignore-vars (s/coll-of symbol?))
(s/def ::api-namespaces (s/coll-of symbol?))
(s/def ::carve-ignore-file string?)
(s/def ::interactive boolean?)
(s/def ::interactive? boolean?) ;; deprecated
(s/def ::dry-run boolean?)
(s/def ::dry-run? boolean?) ;; deprecated
(s/def ::format #{:edn :text :ignore})
(s/def ::aggressive boolean?)
(s/def ::aggressive? boolean?) ;; deprecated
(s/def ::out-dir string?)
(s/def ::report-format (s/keys :req-un [::format]))
(s/def ::report (s/or :bool boolean? :map ::report-format))
(s/def ::silent boolean?)

(s/def ::opts (s/keys :req-un [::paths]
                      :opt-un [::ignore-vars
                               ::api-namespaces
                               ::carve-ignore-file
                               ::interactive
                               ::interactive?
                               ::out-dir
                               ::dry-run
                               ::dry-run?
                               ::aggressive
                               ::aggressive?
                               ::report
                               ::silent]))

(s/fdef carve.api/carve! :args (s/cat :opts ::opts))
