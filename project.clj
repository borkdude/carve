(defproject io.github.borkdude/carve "0.3.5"
  :description "Carve"
  :url "https://github.com/borkdude/carve"
  :scm {:name "git"
        :url  "https://github.com/borkdude/carve"}
  :license {:name "EPL-1.0"
            :url  "https://www.eclipse.org/legal/epl-1.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-kondo/clj-kondo "2023.01.20"]
                 [rewrite-clj/rewrite-clj "1.1.45"]
                 [org.babashka/cli "0.6.44"]]
  :profiles {:test {:dependencies [[org.clojure/test.check "0.10.0"]]}}
  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :username      :env/clojars_user
                                    :password      :env/clojars_pass
                                    :sign-releases false}]])
