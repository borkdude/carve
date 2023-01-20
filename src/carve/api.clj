(ns carve.api
  (:refer-clojure :exclude [run!])
  (:require [carve.impl :as impl]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

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

(defn carve!
  "Similar as main function but with opts already parsed. Use nil opts for passing no opts.
  Intended to be used with clojure -T or clojure -X."
  {:org.babashka/cli
   {:coerce
    {:paths []
     :ignore-vars [:symbol]
     :api-namespaces [:symbol]
     :carve-ignore-file :string
     :interactive :boolean
     :dry-run :boolean
     :format :keyword
     :aggressive :boolean
     :out-dir :string
     :report-format :keyword
     :report :boolean
     :silent :boolean
     :opts :edn}}}
  [opts]
  (let [opts (or (:opts opts) opts)
        config-file (io/file ".carve/config.edn")
        config (when (.exists config-file)
                 (edn/read-string (slurp config-file)))]
    (if (and (empty? opts) (not config))
      (binding [*err* *out*]
        (println "No config found in .carve/config.edn.\nSee https://github.com/borkdude/carve#usage on how to use carve.")
        {:exit-code 1})
      (let [{:keys [:report :config]} (impl/run+ opts)
            format (or (:report-format opts)
                       (-> config :report :format))]
        (when (:report config)
          (impl/print-report report format))
        {:exit-code (if (empty? report) 0 1)}))))
