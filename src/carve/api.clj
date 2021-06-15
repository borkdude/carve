(ns carve.api
  (:refer-clojure :exclude [run!])
  (:require [carve.impl :as impl]))

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
