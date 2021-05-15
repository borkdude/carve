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

(deftest lint-data-test
  (let [uberscript (.getPath (io/file "test-resources" "uberscript" "uberscript.clj"))
        expected '[{:filename "test-resources/uberscript/uberscript.clj",
                    :row      6,
                    :col      1,
                    :ns       medley.core,
                    :name     find-first}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      20,
                    :col      1,
                    :ns       medley.core,
                    :name     dissoc-in}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      38,
                    :col      1,
                    :ns       medley.core,
                    :name     assoc-some}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      47,
                    :col      1,
                    :ns       medley.core,
                    :name     update-existing}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      63,
                    :col      1,
                    :ns       medley.core,
                    :name     update-existing-in}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      88,
                    :col      1,
                    :ns       medley.core,
                    :name     map-entry}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      95,
                    :col      1,
                    :ns       medley.core,
                    :name     map-kv}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      102,
                    :col      1,
                    :ns       medley.core,
                    :name     map-keys}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      107,
                    :col      1,
                    :ns       medley.core,
                    :name     map-vals}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      123,
                    :col      1,
                    :ns       medley.core,
                    :name     map-kv-keys}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      130,
                    :col      1,
                    :ns       medley.core,
                    :name     map-kv-vals}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      155,
                    :col      1,
                    :ns       medley.core,
                    :name     remove-kv}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      161,
                    :col      1,
                    :ns       medley.core,
                    :name     remove-keys}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      167,
                    :col      1,
                    :ns       medley.core,
                    :name     remove-vals}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      173,
                    :col      1,
                    :ns       medley.core,
                    :name     queue}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      180,
                    :col      1,
                    :ns       medley.core,
                    :name     queue?}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      187,
                    :col      1,
                    :ns       medley.core,
                    :name     boolean?}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      194,
                    :col      1,
                    :ns       medley.core,
                    :name     least}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      202,
                    :col      1,
                    :ns       medley.core,
                    :name     greatest}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      210,
                    :col      1,
                    :ns       medley.core,
                    :name     join}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      218,
                    :col      1,
                    :ns       medley.core,
                    :name     deep-merge}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      233,
                    :col      1,
                    :ns       medley.core,
                    :name     mapply}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      252,
                    :col      1,
                    :ns       medley.core,
                    :name     interleave-all}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      271,
                    :col      1,
                    :ns       medley.core,
                    :name     distinct-by}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      298,
                    :col      1,
                    :ns       medley.core,
                    :name     dedupe-by}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      317,
                    :col      1,
                    :ns       medley.core,
                    :name     take-upto}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      336,
                    :col      1,
                    :ns       medley.core,
                    :name     drop-upto}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      352,
                    :col      1,
                    :ns       medley.core,
                    :name     indexed}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      366,
                    :col      1,
                    :ns       medley.core,
                    :name     insert-nth}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      391,
                    :col      1,
                    :ns       medley.core,
                    :name     remove-nth}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      412,
                    :col      1,
                    :ns       medley.core,
                    :name     replace-nth}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      433,
                    :col      1,
                    :ns       medley.core,
                    :name     abs}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      460,
                    :col      1,
                    :ns       medley.core,
                    :name     deref-reset!}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      466,
                    :col      1,
                    :ns       medley.core,
                    :name     ex-message}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      475,
                    :col      1,
                    :ns       medley.core,
                    :name     ex-cause}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      484,
                    :col      1,
                    :ns       medley.core,
                    :name     uuid?}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      489,
                    :col      1,
                    :ns       medley.core,
                    :name     uuid}
                   {:filename "test-resources/uberscript/uberscript.clj",
                    :row      497,
                    :col      1,
                    :ns       medley.core,
                    :name     random-uuid}]]
    (is (= expected
           (main/lint-data {:paths [uberscript]}))
        "Returns (by default) lint data as a vanilla data structure")
    (is (= ""
           (with-out-str (main/lint-data {:paths [uberscript]})))
        "Prints nothing during operation")))

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

(deftest carve-at-end-of-input
  ;; Spit test files instead of pre-creating them under test-resources to avoid chance of
  ;; some editors accidentally stripping trailing newlines on save.
  ;; Spitting under target instead of system temp dir because carve has certain expectations
  ;; on relative paths.
  (let [rel-dir "target/carve-test/carve-at-end-of-input"
        base-expected (str "(ns some.ns.here)\n"
                           "\n"
                           "(deftest a-test\n"
                           "  (testing \"FIXME, I fail.\")\n"
                           "    (is (= 0 1)))")
        base-input (str base-expected "\n"
                        "\n"
                        "(defn i-will-be-carved[])")]
    (.mkdirs (io/file rel-dir))
    (testing "when no trailing newline, no newline should appear at end of input"
      (let [src-file (str (io/file rel-dir "eoi_no_trailing.clj"))]
        (spit src-file base-input)
        (run-main {:paths [src-file] :interactive? false})
        (is (= base-expected (slurp src-file)))))
    (testing "when one trailing newline, one newline should be preserved at end of input"
      (let [src-file (str (io/file rel-dir "eoi_one_trailing.clj"))
            input (str base-input "\n")
            expected (str base-expected "\n")]
        (spit src-file input)
        (run-main {:paths [src-file] :interactive? false})
        (is (= expected (slurp src-file)))))
    (testing "when trailing newlines, one newline should be preserved at end of input"
      (let [src-file (str (io/file rel-dir "eoi_many_trailing.clj"))
            input (str base-input "\n\n\n\n")
            expected (str base-expected "\n")]
        (spit src-file input)
        (run-main {:paths [src-file] :interactive? false})
        (is (= expected (slurp src-file)))))))
