(ns delete-empty-files.all-vars-carved+comment-form-left
  (:require [clojure.string :refer [split]]))

(defn unused-function []) ;; this one is required for the file to be considered for deletion

(comment
  "a comment form should not block the file deletion")

(comment
  "there may be multiple comment forms left in the file")
