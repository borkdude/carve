(ns carve.impl.uberscript
  {:no-doc true}
  (:refer-clojure :exclude [run!])
  (:require [clj-kondo.core :as clj-kondo]
            [clojure.java.io :as io]
            [clojure.string :as str]))

;; clojure -A:carve-local -m carve.main --opts "{:uberscript {:out \"foo.clj\" :main app.core} :paths [\"$CP\"]}"

(defn analyze [main paths]
  (let [{:keys [:namespace-definitions
                :namespace-usages]} (:analysis (clj-kondo/run! {:lint paths
                                                                :config {:output {:analysis true
                                                                                  :canonical-paths true}}}))
        defs-by-name (group-by :name namespace-definitions)
        usage-by-name (group-by :from namespace-usages)
        main-entry (get usage-by-name main)]
    (loop [usages main-entry
           files []]
      (if usages
        (let [[usage & usages] usages
              {:keys [:to :filename]} usage
              to-usages (or (get usage-by-name to)
                            (get defs-by-name to))
              usages (not-empty (into (vec to-usages) usages))
              files (conj files filename)]
          (recur usages files))
        (-> files dedupe reverse)))))

(defn slurp-resource [path]
  (if (str/includes? path "jar")
    ;; TODO: fix this in clj-kondo
    (let [jar-path (str "jar:file:" (str/replace path ":" "!/"))]
      (slurp jar-path))
    (slurp path)))

;;  (slurp (java.net.URI. "jar:file:/Users/borkdude/.m2/repository/medley/medley/1.2.0/medley-1.2.0.jar!/medley/core.cljc"))

(defn run! [{:keys [:paths :uberscript]}]
  (let [{:keys [:out :main]} uberscript
        paths (analyze main paths)
        sources (map slurp-resource paths)
        source (str/join "\n\n" sources)
        source (str source "\n(apply -main *command-line-args*)")]
    (spit out source)))
