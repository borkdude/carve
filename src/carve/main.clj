(ns carve.main
  (:require
   [carve.impl :as impl]
   [clj-kondo.core :as clj-kondo]
   [clojure.edn :as edn]
   [clojure.set :as set]))

(set! *warn-on-reflection* true)

(defn -main [& [flag opts & _args]]
  (when-not (= "--opts" flag)
    (throw (ex-info (str "Unrecognized option: " flag) {:flag flag})))
  (let [opts (edn/read-string opts)
        opts (update opts :api-namespaces set)
        opts (update opts :carve-ignore-file
                     (fn [ci]
                       (if (nil? ci) ".carve_ignore"
                           ci)))
        {:keys [:ignore-vars
                :paths
                :carve-ignore-file
                :aggressive?]} opts]
    (when (empty? (:paths opts)) (throw (ex-info ":paths must not be empty" opts)))
    (loop [removed #{}]
      (let [ignore-from-config (impl/read-carve-ignore-file carve-ignore-file)
            ignore-from-config (map (fn [ep]
                                      [(symbol (namespace ep)) (symbol (name ep))])
                                    ignore-from-config)
            ignore (map (fn [ep]
                          [(symbol (namespace ep)) (symbol (name ep))])
                        ignore-vars)
            analysis (:analysis (clj-kondo/run! {:lint paths
                                                 :config {:output {:analysis true}}}))
            {:keys [:var-definitions :var-usages]} analysis
            var-usages (remove :recursive var-usages)
            definitions-by-ns+name (impl/index-by (juxt :ns :name) var-definitions)
            defined-vars (set (map (juxt :ns :name) var-definitions))
            used-vars (set (map (juxt :to :name) var-usages))
            used-vars (reduce into used-vars [ignore-from-config ignore removed])
            unused-vars (set/difference defined-vars used-vars)
            unused-vars-data (map definitions-by-ns+name unused-vars)]
        (when (seq unused-vars-data)
          (let [data-by-file (group-by :filename unused-vars-data)]
            (doseq [[file vs] data-by-file]
              (impl/carve file vs opts)))
          (when aggressive?
            (recur (into removed unused-vars))))))))
