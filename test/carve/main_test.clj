(ns carve.main-test
  (:require
   [carve.main :as main]
   [clojure.test :as t :refer [deftest is testing]]
   [clojure.string :as str]
   [clojure.java.io :as io]))

(deftest carve-test
  (let [uberscript (.getPath (io/file "test-resources" "uberscript" "uberscript.clj"))
        uberscript-carved-expected (.getPath (io/file "test-resources" "uberscript" "uberscript_carved.clj"))
        tmp-dir (System/getProperty "java.io.tmpdir")
        uberscript-carved (io/file tmp-dir "test-resources" "uberscript" "uberscript.clj")]
    (with-out-str
      (main/-main "--opts" (str {:paths [uberscript]
                                 :aggressive? true
                                 :interactive? false
                                 :out-dir tmp-dir})))
    (is (= (slurp uberscript-carved-expected)
           (slurp uberscript-carved)))))

(deftest issue-11-test
  (let [tmp-dir (System/getProperty "java.io.tmpdir")]
    (with-out-str
      (main/-main "--opts"
                  (str {:paths [(.getPath (io/file "test-resources" "issue_11"))]
                        :aggressive? true
                        :interactive? false
                        :out-dir tmp-dir})))
    (is (= (slurp (io/file "test-resources" "issue_11" "issue_11_expected.clj"))
           (slurp (io/file tmp-dir "test-resources" "issue_11" "issue_11.clj"))))))

(deftest text-report-test
  (is (= (str/trim "
test-resources/app/api.clj:3:1 api/private-lib-function
test-resources/app/app.clj:4:1 app/unused-function
test-resources/app/app.clj:5:1 app/another-unused-function")
         (str/trim (with-out-str
                     (main/-main "--opts"
                                 (str {:paths [(.getPath (io/file "test-resources" "app"))]
                                       :ignore-vars ['app/-main]
                                       :api-namespaces ['api]
                                       :report {:format :text}})))))))

(deftest options-validation-test
  (testing "Forgetting to quote paths give an error"
    (is (thrown? clojure.lang.ExceptionInfo
                 (main/-main "--opts" "{:paths [src test]}"))))

  (testing "Passing a non existing directory fails to validate"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"Path not found"
         (main/-main "--opts" (str {:paths ["not-existing"]}))))))
