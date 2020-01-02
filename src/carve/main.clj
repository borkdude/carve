(ns carve.main
  (:require
   [carve.impl :as impl]
   [clojure.edn :as edn]))

(set! *warn-on-reflection* true)

(defn -main [& [flag opts & _args]]
  (when-not (= "--opts" flag)
    (throw (ex-info (str "Unrecognized option: " flag) {:flag flag})))
  (let [opts (edn/read-string opts)
        report (impl/run! opts)]
    (when-let [r (:report opts)]
      (let [format (:format r)]
        (impl/print-report report format)))))
