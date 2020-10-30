(ns interactive.leave-function
  (:require [clojure.string :refer []]))

(defn used-fn [] nil)

(defn unused-fn [] nil)

(fn -main [& _args]
  (used-fn))
