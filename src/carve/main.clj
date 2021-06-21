(ns carve.main
  (:require
   [carve.impl :as impl]
   [clojure.edn :as edn]
   [clojure.java.io :as io])
  (:gen-class))

(defn main
  [& [flag opts & _args]]
  (let [config-file (io/file ".carve/config.edn")
        config (when (.exists config-file)
                 (edn/read-string (slurp config-file)))]
    (if (and (not flag) (not config))
      (binding [*err* *out*]
        (println "No config found in .carve/config.edn.\nSee https://github.com/borkdude/carve#usage on how to use carve.")
        1)
      (do (when (and (not (= "--opts" flag)) (not config))
            (throw (ex-info (str "Unrecognized option: " flag) {:flag flag})))
          (let [opts (edn/read-string opts)
                {:keys [:report :config]} (impl/run+ opts)
                format (-> config :report :format)]
            (when (:report config)
              (impl/print-report report format))
            (if (empty? report) 0 1))))))

(defn -main
  [& options]
  (System/exit (apply main options)))
