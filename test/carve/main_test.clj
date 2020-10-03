(ns carve.main-test
  (:require
   [carve.main :as main]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as g]
   [clojure.string :as str]
   [clojure.test :as t :refer [deftest is testing]]))

(defmethod clojure.test/report :begin-test-var [m]
  (println "===" (-> m :var meta :name))
  (println))

(defn- run-main [opts]
  (with-out-str
    (main/main "--opts" (str opts))))

(deftest carve-test
  (let [uberscript (.getPath (io/file "test-resources" "uberscript" "uberscript.clj"))
        uberscript-carved-expected (.getPath (io/file "test-resources" "uberscript" "uberscript_carved.clj"))
        tmp-dir (System/getProperty "java.io.tmpdir")
        uberscript-carved (io/file tmp-dir "test-resources" "uberscript" "uberscript.clj")]
    (run-main {:paths [uberscript]
               :aggressive? true
               :interactive? false
               :out-dir tmp-dir})
    (is (= (slurp uberscript-carved-expected)
           (slurp uberscript-carved)))))

(deftest issue-11-test
  (let [tmp-dir (System/getProperty "java.io.tmpdir")]
    (run-main {:paths [(.getPath (io/file "test-resources" "issue_11"))]
                        :aggressive? true
                        :interactive? false
               :out-dir tmp-dir})
    (is (= (slurp (io/file "test-resources" "issue_11" "issue_11_expected.clj"))
           (slurp (io/file tmp-dir "test-resources" "issue_11" "issue_11.clj"))))))

(deftest ignore-main-test
  (is (false?
       (clojure.string/includes?
        (run-main {:paths [(.getPath (io/file "test-resources" "app"))]
                   :api-namespaces ['api]
                   :report {:format :text}})
        "-main"))))

(deftest ignore-var-test
  (is (false?
       (clojure.string/includes?
        (run-main {:paths [(.getPath (io/file "test-resources" "app"))]
                   :api-namespaces ['api]
                   :ignore-vars ['app/ignore-me]
                   :report {:format :text}})
        "-ignore-me"))))

(deftest text-report-test
  (is (= (str/trim "
test-resources/app/api.clj:3:1 api/private-lib-function
test-resources/app/app.clj:4:1 app/unused-function
test-resources/app/app.clj:5:1 app/another-unused-function
test-resources/app/app.clj:9:1 app/ignore-me")
         (str/trim (run-main {:paths [(.getPath (io/file "test-resources" "app"))]
                              :api-namespaces ['api]
                              :report {:format :text}})))))

(deftest report-exit-code-test
  (testing "Nothing to report exits with exit code 0"
    (is (= 0 (main/main "--opts" (str {:paths [(.getPath (io/file "test-resources" "unchanged.clj"))]
                                       :api-namespaces ['api]
                                       :report {:format :text}})))))

  (testing "Something to report exits with exit code 1"
    (is (= 1
           (main/main "--opts" (str {:paths [(.getPath (io/file "test-resources" "app"))]
                                     :api-namespaces ['api]
                                     :report {:format :text}}))))))

(deftest options-validation-test
  (testing "Forgetting to quote paths give an error"
    (is (thrown? clojure.lang.ExceptionInfo
                 (run-main "{:paths [src test]}"))))

  (testing "Generate random options validate"
    (doseq [o (g/sample (s/gen ::main/opts))]
      ;; the paths needs to exist to to simplify the test we just hard code it
      (is (nil? (main/validate-opts! (assoc o :paths ["."]))))))

  (testing "Passing a non existing directory fails to validate"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"Path not found"
         (run-main {:paths ["not-existing"]})))))
