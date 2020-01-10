(ns carve.main
  (:require
   [carve.impl :as impl]
   [carve.impl.uberscript :as uberscript]
   [clojure.edn :as edn]))

(set! *warn-on-reflection* true)

(defn -main [& [flag opts & _args]]
  (when-not (= "--opts" flag)
    (throw (ex-info (str "Unrecognized option: " flag) {:flag flag})))
  (let [opts (edn/read-string opts)
        uberscript (:uberscript opts)]
    (if uberscript
      (do
        (println "Uberscripting" (:out uberscript))
        (uberscript/run! opts)
        (println "Carving" (:out uberscript))
        (impl/run! (assoc opts
                          :paths [(:out uberscript)]
                          :interactive? false
                          :aggressive? true)))
      (let [report (impl/run! opts)]
        (when-let [r (:report opts)]
          (let [format (:format r)]
            (impl/print-report report format)))))))
