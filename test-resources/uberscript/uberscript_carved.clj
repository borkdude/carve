(ns medley.core
  "A small collection of useful, mostly pure functions that might not look out
  of place in the clojure.core namespace."
  (:refer-clojure :exclude [boolean? ex-cause ex-message uuid uuid? random-uuid]))

(defn index-by
  "Returns a map of the elements of coll keyed by the result of f on each
  element. The value at each key will be the last element in coll associated
  with that key. This function is similar to `clojure.core/group-by`, except
  that elements with the same key are overwritten, rather than added to a
  vector of values."
  {:added "1.2.0"}
  [f coll]
  (persistent! (reduce #(assoc! %1 (f %2) %2) (transient {}) coll)))

(require '[medley.core :refer [index-by]]) (index-by :id [{:id 1} {:id 2}])

(defn -main [& args]
  (println "in main"))
