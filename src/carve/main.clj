(ns carve.main
  (:require
   [carve.impl :as impl]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [expound.alpha :as expound])
  (:gen-class))

(set! *warn-on-reflection* true)
(s/check-asserts true)

(s/def ::paths (s/coll-of string?))
(s/def ::ignore-vars (s/coll-of symbol?))
(s/def ::api-namespaces (s/coll-of symbol?))
(s/def ::carve-ignore-file string?)
(s/def ::interactive boolean?)
(s/def ::interactive? boolean?) ;; deprecated
(s/def ::dry-run boolean?)
(s/def ::dry-run? boolean?) ;; deprecated
(s/def ::format #{:edn :text})
(s/def ::aggressive boolean?)
(s/def ::aggressive? boolean?) ;; deprecated
(s/def ::out-dir string?)
(s/def ::report-format (s/keys :req-un [::format]))
(s/def ::report (s/or :bool boolean? :map ::report-format))
(s/def ::silent boolean?)

(s/def ::opts (s/keys :req-un [::paths]
                      :opt-un [::ignore-vars
                               ::api-namespaces
                               ::carve-ignore-file
                               ::interactive
                               ::interactive?
                               ::out-dir
                               ::dry-run
                               ::dry-run?
                               ::aggressive
                               ::aggressive?
                               ::report
                               ::silent]))

(defn- valid-path?
  [p]
  (.exists (io/file p)))

(defn validate-opts!
  "Validate options throwing an exception if they don't validate"
  [{:keys [paths] :as opts}]
  (binding [s/*explain-out* expound/printer]
    (s/assert ::opts opts))
  (when-not (every? valid-path? paths)
    (throw (ex-info "Path not found" {:paths paths}))))

(defn- load-opts
  "Load options, giving higher precedence to options passed from the CLI"
  [config opts]
  (let [opts (if opts
               (edn/read-string opts)
               config)]
    (validate-opts! opts)
    opts))

(defn main
  [& [flag opts & _args]]
  (let [config-file (io/file ".carve/config.edn")
        config (when (.exists config-file)
                 (edn/read-string (slurp config-file)))]
    (if (and (not flag) (not config))
      (binding [*err* *out*]
        (println "No config found in .carve/config.edn.\nSee https://github.com/borkdude/carve#usage on how to use carve.")
        1)
      (do (when (and (not (= "--opts" flag)) (not config))
            (throw (ex-info (str "Unrecognized option: " flag) {:flag flag})))
          (let [opts (load-opts config opts)
                format (-> opts :report :format)
                report (impl/run! opts)]
            (when format
              (impl/print-report report format))
            (if (empty? report) 0 1))))))

(defn -main
  [& options]
  (System/exit (apply main options)))
