(ns carve.api
  (:refer-clojure :exclude [run!])
  (:require [carve.impl :as impl]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [babashka.cli :as cli]))

(defn print! [{:keys [:report :config]}]
  (let [format (-> config :report :format)]
    (impl/print-report report format)))

(defn run!
  "Returns unused vars as EDN data in :report. The entire return value
  may be passed to print! for printing. Accepts similar options as the
  CLI."
  ([] (run! nil))
  ([opts]
   (impl/run+ opts)))

(defn report
  "Similar to run! but forces non-side-effecting non-interactive behavior."
  ([] (report {:merge-config true}))
  ([opts]
   (impl/run+ (assoc opts :report true))))

(def cli-opts
  {:spec
   {:paths {:coerce [] :desc "A list of paths to analyze (files and dirs)"}
    :ignore-vars {:coerce [:symbol] :desc "A list of vars to ignore"}
    :api-namespaces {:coerce [:symbol] :desc "A list of namespaces to ignore"}
    :carve-ignore-file {:coerce :string :desc "The file with ignored vars"}
    :interactive {:coerce :boolean :desc "Interactive mode: ask what to do with an unused var"}
    :dry-run {:coerce :boolean :desc "Dry run "}
    :aggressive {:coerce :boolean :desc "Run carve multiple times to detect transitive unused vars"}
    :out-dir {:coerce :string :desc "Emit transformed code to out-dir instead of overwriting"}
    :delete-empty-files {:coerce :boolean :desc "When truthy, also deletes files considered empty"}
    :report-format {:coerce :keyword :desc "The report format: :text, :edn or :ignore"}
    :report {:coerce :boolean :desc "Set to true to report and not transform code"}
    :silent {:coerce :boolean :desc "When truthy, does not write to stdout. Implies :interactive false."}
    :opts {:coerce :edn :desc "The options as an EDN literal"}
    :clj-kondo/config {:coerce :edn :desc "A map of clj-kondo config opts that are passed on to clj-kondo"}
    :help {:coerce :boolean :desc "Display help"}}})

(defn print-help []
  (println "Carve: remove unused Clojure vars.")
  (println)
  (println (cli/format-opts cli-opts)))

(defn carve!
  "Similar as main function but with opts already parsed. Use nil opts for passing no opts.
  Intended to be used with clojure -T or clojure -X."
  {:org.babashka/cli cli-opts}
  [opts] ;; to validate opts with spec, load carve.specs
  (let [opts (or (:opts opts) opts)
        config-file (io/file ".carve/config.edn")
        config (when (.exists config-file)
                 (edn/read-string (slurp config-file)))]
    (if (and (empty? opts) (not config))
      (binding [*err* *out*]
        (print-help)
        {:exit-code 1})
      (if (:help opts)
        (do (print-help)
            {:exit-code 0})
        (let [{:keys [:report :config]} (impl/run+ opts)
                format (or (:report-format opts)
                           (-> config :report :format))]
            (when (:report config)
              (impl/print-report report format))
            {:exit-code (if (empty? report) 0 1)})))))
