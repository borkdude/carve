(ns carve.main
  (:require
   [carve.api :as api]
   [babashka.cli :as cli])
  (:gen-class))

(defn main
  [& args]
  (let [opts (cli/parse-opts args api/cli-opts)]
    (:exit-code (api/carve! opts))))

(defn -main
  [& options]
  (System/exit (apply main options)))
