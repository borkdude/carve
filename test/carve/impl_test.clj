(ns carve.impl-test
  (:require [carve.impl :as impl]
            [clojure.test :as t :refer [deftest is testing]]
            [clojure.java.io :as io]))

(deftest impl-test []
  (testing "without aggressive"
    (is (= '[{:filename "test-resources/app/app.clj", :row 4, :col 1, :ns app, :name unused-function}
             {:filename "test-resources/app/app.clj", :row 5, :col 1, :ns app, :name another-unused-function}
             {:filename "test-resources/app/api.clj", :row 3, :col 1, :ns api, :name private-lib-function}]
           (impl/run! {:paths [(.getPath (io/file "test-resources" "app"))]
                       :ignore-vars ['app/-main] :api-namespaces ['api] :report true}))))
  (testing "with aggressive"
    (= '[{:filename "test-resources/app/app.clj", :row 4, :col 1, :ns app, :name unused-function}
         {:filename "test-resources/app/app.clj", :row 5, :col 1, :ns app, :name another-unused-function}
         {:filename "test-resources/app/api.clj", :row 3, :col 1, :ns api, :name private-lib-function}
         {:filename "test-resources/app/app.clj", :row 3, :col 1, :ns app, :name only-used-by-unused-function}]
       (impl/run! {:paths [(.getPath (io/file "test-resources" "app"))]
                   :ignore-vars ['app/-main] :api-namespaces ['api] :report true :aggressive? true}))))
