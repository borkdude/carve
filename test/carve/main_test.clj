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
    (run-main {:paths        [(.getPath (io/file "test-resources" "issue_11"))]
               :aggressive?  true
               :interactive? false
               :out-dir      tmp-dir})
    (is (= (slurp (io/file "test-resources" "issue_11" "issue_11_expected.clj"))
           (slurp (io/file tmp-dir "test-resources" "issue_11" "issue_11.clj"))))))

(deftest remove-refers-test
  (let [tmp-dir (System/getProperty "java.io.tmpdir")]
    (run-main {:paths        [(.getPath (io/file "test-resources" "remove_refers" "uberscript.clj"))]
               :aggressive?  true
               :interactive? false
               :out-dir      tmp-dir})
    (is (= (str/split (slurp (io/file "test-resources" "remove_refers" "uberscript_expected.clj"))
                      #"\n")
           (str/split (slurp (io/file tmp-dir "test-resources" "remove_refers" "uberscript.clj"))
                      #"\n")))))

(deftest remove-refers-test-two
  (testing "unused refers in multiple namespaces"
    (let [tmp-dir (System/getProperty "java.io.tmpdir")]
      (run-main {:paths        [(.getPath (io/file "test-resources" "remove_refers" "uberscript_two.clj"))]
                 :aggressive?  true
                 :interactive? false
                 :out-dir      tmp-dir})
      (is (= (str/split (slurp (io/file "test-resources" "remove_refers" "uberscript_two_expected.clj"))
                        #"\n")
             (str/split (slurp (io/file tmp-dir "test-resources" "remove_refers" "uberscript_two.clj"))
                        #"\n"))))))

(deftest ignore-main-test
  (is (false?
        (clojure.string/includes?
          (run-main {:paths          [(.getPath (io/file "test-resources" "app"))]
                     :api-namespaces ['api]
                     :report         {:format :text}})
          "-main"))))

(deftest unused-arrow-fn-names-test
  (is (clojure.string/includes?
        (run-main {:paths          [(.getPath (io/file "test-resources" "app"))]
                   :api-namespaces ['api]
                   :report         {:format :text}})
        "->unused-arrow-fn")))

(deftest ignore-var-test
  (is (false?
        (clojure.string/includes?
          (run-main {:paths          [(.getPath (io/file "test-resources" "app"))]
                     :api-namespaces ['api]
                     :ignore-vars    ['app/ignore-me]
                     :report         {:format :text}})
          "-ignore-me"))))


(deftest text-report-test
  (is (=
        (-> (str/trim "
test-resources/app/api.clj:3:1 api/private-lib-function
test-resources/app/app.clj:4:1 app/unused-function
test-resources/app/app.clj:5:1 app/another-unused-function
test-resources/app/app.clj:9:1 app/ignore-me
test-resources/app/app.clj:11:1 app/->unused-arrow-fn
") (str/split #"\n"))
        (-> (str/trim (run-main {:paths          [(.getPath (io/file "test-resources" "app"))]
                                 :api-namespaces ['api]
                                 :report         {:format :text}}))
            (str/split #"\n")))))

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

(deftest comment-only-usage-test
  (testing "tests reporting/removal of functions only used in comment"
    (is (clojure.string/includes?
          (run-main {:paths            [(.getPath (io/file "test-resources" "app"))]
                     :api-namespaces   ['api]
                     :clj-kondo/config {:skip-comments true}
                     :report           {:format :text}})
          "only-used-in-comment"))))

(deftest interactive-tests
  (testing "pressing `Y` removes vars as expected"
    (with-in-str "Y\nY\n"
      (let [tmp-dir (System/getProperty "java.io.tmpdir")]
        (run-main {:paths        [(.getPath (io/file "test-resources" "interactive" "remove_all.clj"))]
                   :interactive? true
                   :out-dir      tmp-dir})
        (is (= (str/split (slurp (io/file "test-resources" "interactive" "remove_all_expected.clj"))
                          #"\n")
               (str/split (slurp (io/file tmp-dir "test-resources" "interactive" "remove_all.clj"))
                          #"\n"))))))

  (testing "Yes and then `skip` (pressing `return`) removes the first but not second var"
    (with-in-str "Y\n" ;; empty
      (let [tmp-dir (System/getProperty "java.io.tmpdir")]
        (run-main {:paths        [(.getPath (io/file "test-resources" "interactive" "leave_function.clj"))]
                   :interactive? true
                   :out-dir      tmp-dir})
        (is (= (str/split (slurp (io/file "test-resources" "interactive" "leave_function_expected.clj"))
                          #"\n")
               (str/split (slurp (io/file tmp-dir "test-resources" "interactive" "leave_function.clj"))
                          #"\n")))))))

(deftest interactive-refer-removal-context
  (testing "removing a referral interactively shows some context"
    (with-in-str "" ;; empty string is effectively the user hitting `return` to all prompts
      (is (clojure.string/includes?
            (run-main {:paths       [(.getPath (io/file "test-resources" "app"))]
                       :dry-run     true
                       :interactive true})
            ;; perhaps this is too exact a match
            "Found unused var:
------------------
1: (ns app (:require [clojure.string :refer [split]]))
                                             ^--- unused var")))))

(deftest load-config-test
  (testing "passing arguments from the cli overrides the configuration"
    (is (= {:paths ["src"]}
           (#'main/load-opts {:paths ["src" "test"]}
                             "{:paths [\"src\"]}")))))
