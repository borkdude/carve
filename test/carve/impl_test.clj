(ns carve.impl-test
  (:require [carve.impl :as impl]
            [clojure.java.io :as io]
            [clojure.test :as t :refer [deftest is testing]]))

(deftest impl-test []
  (testing "without aggressive"
    (is (= '#{{:filename "test-resources/app/api.clj"
               :row      3
               :col      1
               :ns       api
               :name     private-lib-function}
              {:filename "test-resources/app/app.clj"
               :row      4
               :col      1
               :ns       app
               :name     unused-function}
              {:filename "test-resources/app/app.clj"
               :row      5
               :col      1
               :ns       app
               :name     another-unused-function}
              {:filename "test-resources/app/app.clj"
               :row      11
               :col      1
               :ns       app
               :name     ->unused-arrow-fn}
              {:filename "test-resources/app/app.clj"
               :row      1
               :col      43
               :ns       clojure.string
               :name     split}}
           (set (impl/run! {:paths          [(.getPath (io/file "test-resources" "app"))]
                            :ignore-vars    ['app/-main 'app/ignore-me]
                            :api-namespaces ['api]
                            :report         true})))))

  (testing "with aggressive"
    (is (= '#{{:filename "test-resources/app/api.clj",
               :row      3,
               :col      1,
               :ns       api,
               :name     private-lib-function}
              {:filename "test-resources/app/app.clj",
               :row      4,
               :col      1,
               :ns       app,
               :name     unused-function}
              {:filename "test-resources/app/app.clj",
               :row      5,
               :col      1,
               :ns       app,
               :name     another-unused-function}
              {:filename "test-resources/app/app.clj",
               :row      9,
               :col      1,
               :ns       app,
               :name     ignore-me}
              {:filename "test-resources/app/app.clj",
               :row      3,
               :col      1,
               :ns       app,
               :name     only-used-by-unused-function}
              {:filename "test-resources/app/app.clj"
               :row      11
               :col      1
               :ns       app
               :name     ->unused-arrow-fn}
              {:filename "test-resources/app/app.clj"
               :row      1
               :col      43
               :ns       clojure.string
               :name     split
               }
              }

           (set (impl/run! {:paths       [(.getPath (io/file "test-resources" "app"))]
                            :ignore-vars ['app/-main] :api-namespaces ['api] :report true :aggressive true})))))

  (testing "without aggressive, but skip comment usages"
    (is (= '#{{:filename "test-resources/app/api.clj"
               :row      3
               :col      1
               :ns       api
               :name     private-lib-function}
              {:filename "test-resources/app/app.clj"
               :row      4
               :col      1
               :ns       app
               :name     unused-function}
              {:filename "test-resources/app/app.clj"
               :row      5
               :col      1
               :ns       app
               :name     another-unused-function}
              {:filename "test-resources/app/app.clj"
               :row      11
               :col      1
               :ns       app
               :name     ->unused-arrow-fn}
              {:filename "test-resources/app/app.clj"
               :row      13
               :col      1
               :ns       app
               :name     only-used-in-comment}
              {:filename "test-resources/app/app.clj"
               :row      1
               :col      43
               :ns       clojure.string
               :name     split}}
           (set (impl/run! {:paths            [(.getPath (io/file "test-resources" "app"))]
                            :clj-kondo/config {:skip-comments true}
                            :ignore-vars      ['app/-main 'app/ignore-me]
                            :api-namespaces   ['api] :report true}))))))
