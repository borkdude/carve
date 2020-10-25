(ns app)

(defn only-used-by-unused-function []) ;; only reported with aggressive?
(defn unused-function [] (only-used-by-unused-function))
(defn another-unused-function [])

(defn used-function []) ;; won't be reported because used by -main

(defn ignore-me [] nil)

(defn ->unused-arrow-fn [] nil) ;; should be reported despite leading `-`

(defn -main []
  (used-function))
