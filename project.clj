;; THIS PROJECT.CLJ DOESN'T WORK YET, SEE TODO
(defproject borkude/carve "0.0.1-SNAPSHOT"
  :description "Carve"
  :url "https://github.com/borkdude/carve"
  :scm {:name "git"
        :url "https://github.com/borkdude/carve"}
  :license {:name "EPL-1.0"
            :url "https://www.eclipse.org/legal/epl-1.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-kondo "2020.05.09"]
                 [rewrite-clj "1.0.572-alpha"]]
  :plugins [[reifyhealth/lein-git-down "0.3.6"]]
  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                    :username :env/clojars_user
                                    :password :env/clojars_pass
                                    :sign-releases false}]])
