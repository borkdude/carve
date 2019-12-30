(ns carve.main
  (:require
   [clj-kondo.core :as clj-kondo]
   [carve.impl :as impl]
   [clojure.set :as set]
   [clojure.edn :as edn]
   [clojure.java.io :as io]))

(set! *warn-on-reflection* true)

(defn -main [& [flag opts & _args]]
  (when-not (or (= "-o" flag)
                (= "--opts" flag))
    (throw (ex-info (str "Unrecognized option: " flag) {:flag flag})))
  (let [opts (edn/read-string opts)
        opts (update opts :ignore-namespaces set)
        opts (update opts :carve-ignore-file
                     (fn [ci]
                       (if (nil? ci) ".carve_ignore"
                           ci)))
        {:keys [:ignore-vars
                :paths
                :carve-ignore-file]} opts
        ignore-file (io/file carve-ignore-file)
        _ (when-not (.exists ignore-file) (.createNewFile ignore-file))
        ignore-from-config (edn/read-string (format "[%s]" (slurp carve-ignore-file)))
        ignore-from-config (map (fn [ep]
                                  [(symbol (namespace ep)) (symbol (name ep))])
                                ignore-from-config)
        _ (when (empty? paths) (throw (ex-info ":paths must not be empty" opts)))
        ignore (map (fn [ep]
                    [(symbol (namespace ep)) (symbol (name ep))])
                    ignore-vars)
        analysis (:analysis (clj-kondo/run! {:lint paths
                                             :config {:output {:analysis true}}}))
        {:keys [:var-definitions :var-usages]} analysis
        definitions-by-ns+name (impl/index-by (juxt :ns :name) var-definitions)
        defined-vars (set (map (juxt :ns :name) var-definitions))
        used-vars (set (map (juxt :to :name) var-usages))
        used-vars (reduce into used-vars [ignore-from-config ignore])
        unused-vars (set/difference defined-vars used-vars)
        unused-vars-data (map definitions-by-ns+name unused-vars)
        data-by-file (group-by :filename unused-vars-data)]
    (doseq [[file vs] data-by-file]
      (impl/carve file vs opts))))
