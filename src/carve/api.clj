(ns carve.api
  (:refer-clojure :exclude [run!])
  (:require [carve.impl :as impl]))

(defn report
  "Returns unused vars as EDN data. Accepts similar options as the CLI,
  but defaults to non-interactive and non-side effecting behavior."
  ([] (report {:merge-config true}))
  ([opts]
   {:unused-vars (impl/run! (assoc opts :report true))}))
