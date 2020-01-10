(ns app.impl
  (:require [medley.core :refer [index-by map-keys]]))

(defn impl []
  (->> [{:id 1} {:id 2} {:id 3}]
       (index-by :id)
       (map-keys inc)))
