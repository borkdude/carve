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
(s/def ::ignore-vars (s/coll-of symbol?))
(s/def ::api-namespaces (s/coll-of symbol?))
(s/def ::carve-ignore-file string?)
(s/def ::interactive? boolean?)
(s/def ::dry-run? boolean?)
(s/def ::format #{:edn :text})
(s/def ::aggressive? boolean?)
(s/def ::out-dir string?)
(s/def ::report-format (s/keys :req-un [::format]))
(s/def ::report (s/or :bool boolean? :map ::report-format))

(s/def ::opts (s/keys :req-un [::paths]
                      :opt-un [::ignore-vars
                               ::api-namespaces
                               ::carve-ignore-file
                               ::interactive?
                               ::out-dir
                               ::dry-run?
                               ::aggressive?
                               ::report]))

(defn- valid-path?
  [p]
  (.exists (io/file p)))

(defn validate-opts!
  "Validate options throwing an exception if they don't validate"
  [{:keys [paths] :as opts}]
  (s/assert ::opts opts)
  (when-not (every? valid-path? paths)
    (throw (ex-info "Path not found" {:paths paths}))))

(defn main
  [& [flag opts & _args]]
  (when-not (= "--opts" flag)
    (throw (ex-info (str "Unrecognized option: " flag) {:flag flag})))
  (let [opts (edn/read-string opts)
        _ (validate-opts! opts)
        format (-> opts :report :format)
        report (impl/run! opts)]
    (when format
      (impl/print-report report format))
    (if (empty? report) 0 1)))

(defn -main
  [& options]
  (System/exit (apply main options)))
