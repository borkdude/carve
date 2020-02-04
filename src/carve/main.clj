(ns carve.main
  (:require
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [carve.impl :as impl]
   [clojure.edn :as edn]
   [expound.alpha :as expound]))

(set! *warn-on-reflection* true)
(set! s/*explain-out* expound/printer)
(s/check-asserts true)

(s/def ::paths (s/coll-of string?))
(s/def ::opts (s/keys :req-un [::paths]))

(defn- valid-path?
  [p]
  (.exists (io/file p)))

(defn- validate-opts!
  "Validate options throwing an exception if they don't validate"
  [{:keys [paths] :as opts}]
  (s/assert ::opts opts)
  (when-not (every? valid-path? paths)
    (throw (ex-info "Path not found" {:paths paths}))))

(defn -main [& [flag opts & _args]]
  (when-not (= "--opts" flag)
    (throw (ex-info (str "Unrecognized option: " flag) {:flag flag})))
  (let [opts (edn/read-string opts)
        _ (validate-opts! opts)
        report (impl/run! opts)]
    (when-let [r (:report opts)]
      (let [format (:format r)]
        (impl/print-report report format)))))
