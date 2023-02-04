;; comment before other content - should not interfere with deletion

(ns delete-empty-files.all-vars-carved
  (:require [clojure.string :refer [split]]))

;; at least 1 unused fn is required for the file to be deleted

(defn unused-function [])
(defn another-unused-function [])

;; comment after other content - should not interfere with deletion
