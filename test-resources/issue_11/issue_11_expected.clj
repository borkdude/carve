(require '[clojure.test :as t])

(defn- private-fn [])

(t/deftest foo
  (private-fn))
