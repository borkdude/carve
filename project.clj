(defproject borkdude/carve "0.0.1-SNAPSHOT"
  :description "Carve"
  :url "https://github.com/borkdude/carve"
  :scm {:name "git"
        :url  "https://github.com/borkdude/carve"}
  :license {:name "EPL-1.0"
            :url  "https://www.eclipse.org/legal/epl-1.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-kondo/clj-kondo "2021.04.23"]
                 [rewrite-clj/rewrite-clj "1.0.572-alpha"]
                 [expound/expound "0.8.6"]]
  :profiles {:test {:dependencies [[org.clojure/test.check "0.10.0"]]}}
  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :username      :env/clojars_user
                                    :password      :env/clojars_pass
                                    :sign-releases false}]])
