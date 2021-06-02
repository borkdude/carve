(ns carve.core
  (:refer-clojure :exclude [run!])
  (:require
   [carve.impl :as impl]
   [carve.main :as main]))

(defn run!
  "Programmatic API especially apt for being invoked within an existing, vanilla JVM.

  The defaults are data-oriented and (generally) favor a side-effect-free functionality.

  If you pass `maybe-opts` as map (empty or not), it will be used as the basis for configuration.
  If you pass `nil` instead, a .carve/config.edn will be read and parsed, if it exists.

  In both cases, the mentioned defaults will be assoc'ed for absent keys."
  [maybe-opts]
  (let [{:keys [dry-run interactive silent]
         :or   {dry-run     true
                interactive true
                silent      true}
         :as   opts} (or maybe-opts (main/slurp-config))
        opts (assoc opts
                    :dry-run     dry-run
                    :interactive interactive
                    :silent      silent)
        format-path [:report :format]
        opts (cond-> opts
               (= ::not-found
                  (get-in opts format-path ::not-found)) (assoc-in format-path :edn))]
    (main/validate-opts! opts)
    (impl/run! opts)))
