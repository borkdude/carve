(ns carve.api
  (:refer-clojure :exclude [run!])
  (:require [carve.impl :as impl]))

(defn print! [{:keys [:report :config]}]
  (let [format (-> config :report :format)]
    (impl/print-report (:unused-vars report) format)))

(defn run!
  "Returns unused vars as EDN data in :report. The entire return value
  may be passed to print! for printing. Accepts similar options as the
  CLI."
  ([] (run! nil))
  ([opts]
   (let [{:keys [:report :config]} (impl/run+ opts)]
     {:report {:unused-vars report}
      :config config})))

(defn report
  "Similar to run! but forces non-side-effecting non-interactive behavior."
  ([] (report {:merge-config true}))
  ([opts]
   (let [{:keys [:report :config]} (impl/run+ (assoc opts :report true))]
     {:report {:unused-vars report}
      :config config})))
