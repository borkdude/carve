(ns app.core
  (:require [app.impl :as impl]))

(defn -main []
  (prn (impl/foo)))
