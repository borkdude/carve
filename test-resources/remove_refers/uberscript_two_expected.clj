(ns app.command)

(defn used-fn [] (prn "used-fn called"))

(ns app.another-ns
  (:require
   [app.command :refer []]))

(defn another-used-fn [] (prn "another-used-fn called"))

(ns app.cli
  (:require
   [app.another-ns :refer [another-used-fn]]
   [app.command :refer [used-fn]]))

(defn -main [& _args]
  (another-used-fn)
  (used-fn))

(ns user (:require [app.cli])) (apply app.cli/-main *command-line-args*)
