(ns carve.main
  (:require
   [carve.api :as api]
   [clojure.edn :as edn])
  (:gen-class))

(defn main
  [& [flag opts & _args]]
  (when (not (= "--opts" flag))
    (throw (ex-info (str "Unrecognized option: " flag) {:flag flag})))
  (let [opts (if opts (edn/read-string opts) nil)]
    (:exit-code (api/carve! opts))))

(defn -main
  [& options]
  (System/exit (apply main options)))
