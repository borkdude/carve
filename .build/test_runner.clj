(ns test-runner
  (:require [clojure.test :as t]))

(require '[carve.main-test]
         '[carve.impl-test])

(defn run-tests [_]
  (t/run-tests 'carve.main-test
               'carve.impl-test))
