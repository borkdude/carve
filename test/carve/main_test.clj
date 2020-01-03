(ns carve.main-test
  (:require
   [carve.main :as main]
   [clojure.test :as t :refer [deftest is]]
   [clojure.string :as str]
   [clojure.java.io :as io]))

(deftest carve-test
  (let [uberscript (.getPath (io/file "test-resources" "uberscript" "uberscript.clj"))
        uberscript-carved-expected (.getPath (io/file "test-resources" "uberscript" "uberscript_carved.clj"))
        tmp-dir (System/getProperty "java.io.tmpdir")
        uberscript-carved (io/file tmp-dir "test-resources" "uberscript" "uberscript.clj")]
    (main/-main "--opts" (str {:paths [uberscript]
                               :aggressive? true
                               :interactive? false
                               :out-dir tmp-dir}))
    (is (= (slurp uberscript-carved-expected)
           (slurp uberscript-carved)))))

(deftest text-report-test
  (is (= (str/trim "
test-resources/app/app.clj:4:1 app/unused-function
test-resources/app/app.clj:5:1 app/another-unused-function
test-resources/app/api.clj:3:1 api/private-lib-function")
         (str/trim (with-out-str
                     (main/-main "--opts"
                                 (str {:paths [(.getPath (io/file "test-resources" "app"))]
                                       :ignore-vars ['app/-main]
                                       :api-namespaces ['api]
                                       :report {:format :text}})))))))
