(ns app)

(defn only-used-by-unused-function []) ;; only reported with aggressive?
(defn unused-function [] (only-used-by-unused-function))
(defn another-unused-function [])

(defn used-function []) ;; won't be reported because used by -main
(defn -main []
  (used-function))
