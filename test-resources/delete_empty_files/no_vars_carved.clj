(ns delete-empty-files.no-vars-carved
    (:require [clojure.string :refer [split]]))

(defn used-function []) ;; won't be reported because used by -main

(defn -main []
      (used-function))
