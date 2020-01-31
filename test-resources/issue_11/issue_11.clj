(require '[clojure.test :as t])

(defn- private-unused-fn [])
(defn- private-fn [])

(t/deftest foo
  (private-fn))
