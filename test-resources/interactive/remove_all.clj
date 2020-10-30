(ns interactive.remove-all
  (:require [clojure.string :refer [split]]))

(defn used-fn [] nil)

(defn unused-fn [] nil)

(fn -main [& _args]
  (used-fn))
