(ns interactive.remove-all
  (:require [clojure.string :refer []]))

(defn used-fn [] nil)

(fn -main [& _args]
  (used-fn))
