(defproject borkude/carve "0.0.1-SNAPSHOT"
  :description "Carve"
  :url "https://github.com/borkdude/carve"
  :scm {:name "git"
        :url  "https://github.com/borkdude/carve"}
  :license {:name "EPL-1.0"
            :url  "https://www.eclipse.org/legal/epl-1.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-kondo/clj-kondo "0d31f63e40f220dff88670335ad639d39647314c"]
                 [rewrite-clj/rewrite-clj "1.0.572-alpha"]
                 [expound/expound "0.8.6"]]
  :profiles {:kaocha {:dependencies [[org.clojure/test.check "0.10.0"]
                                     [lambdaisland/kaocha "0.0-590"]
                                     [lambdaisland/kaocha-junit-xml "0.0-70"]
                                     [lambdaisland/kaocha-cloverage "0.0-41"]]}
             :test   {:dependencies [[org.clojure/test.check "0.10.0"]
                                     [com.cognitect/test-runner "209b64504cb3bd3b99ecfec7937b358a879f55c1"]]}}
  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :username      :env/clojars_user
                                    :password      :env/clojars_pass
                                    :sign-releases false}]]
  :plugins [[reifyhealth/lein-git-down "0.4.0"]]
  :middleware         [lein-git-down.plugin/inject-properties]
  :git-down {com.cognitect/test-runner {:coordinates cognitect-labs/test-runner}}
  :repositories [["public-github" {:url "git://github.com"}]
                 ["private-github" {:url "git://github.com" :protocol :ssh}]])
