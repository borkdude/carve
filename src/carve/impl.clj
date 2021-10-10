(ns carve.impl
  {:no-doc true}
  (:refer-clojure :exclude [run!])
  (:require
   [clj-kondo.core :as clj-kondo]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.set :as set]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [expound.alpha :as expound]
   [rewrite-clj.node :as node]
   [rewrite-clj.zip :as z]))

(defn index-by
  [f coll]
  (persistent! (reduce #(assoc! %1 (f %2) %2) (transient {}) coll)))

(defn sanitize-opts [opts]
  (when (empty? (:paths opts)) (throw (ex-info ":paths must not be empty" opts)))
  (let [{:keys [:dry-run? :dry-run :aggressive? :aggressive :interactive? :interactive]} opts
        ;;_ (prn opts)
        opts (assoc opts
                    :dry-run (or dry-run dry-run?)
                    :aggressive (or aggressive aggressive?)
                    :interactive (if-not (or (contains? opts :interactive?)
                                             (contains? opts :interactive))
                                   true
                                   (or interactive interactive?)))
        opts (update opts :api-namespaces set)
        opts (update opts :carve-ignore-file
                     (fn [ci]
                       (if (nil? ci)
                         (if (.exists (io/file ".carve_ignore"))
                           (do
                             (binding [*out* *err*]
                               (println "The .carve_ignore file is deprecated. Please move it to .carve/ignore."))
                             ".carve_ignore")
                           ".carve/ignore")
                         ci)))
        opts (if (:report opts)
               ;; report implies dry-run
               (assoc opts :dry-run true)
               opts)
        opts (if (:dry-run opts)
               (assoc opts :interactive false)
               opts)
        opts (if (:out-dir opts)
               opts
               (assoc opts :out-dir (System/getProperty "user.dir")))
        opts (if (:silent opts)
               (assoc opts :interactive false)
               opts)]
    opts))

(defn read-carve-ignore-file [carve-ignore-file]
  (let [ignore-file (io/file carve-ignore-file)]
    (when (.exists ignore-file)
      (edn/read-string (format "[%s]" (slurp carve-ignore-file))))))

(defn add-to-carve-ignore-file [carve-ignore-file s]
  (let [ignore-file (io/file carve-ignore-file)]
    (when-not (.exists ignore-file)
      (io/make-parents ignore-file)
      (.createNewFile ignore-file))
    (spit carve-ignore-file s :append true)))

(defn interact [{:keys [:carve-ignore-file]} sym]
  (println
    (if sym
      (format "Type Y to remove or i to add %s to %s" sym carve-ignore-file)
      ;; no sym means nothing valid to add to carve-ignore
      "Type Y to remove."))
  (let [input (read-line)]
    (when (and (= "i" input) sym)
      (add-to-carve-ignore-file carve-ignore-file (str sym "\n")))
    input))

(defn loc-context [file [row col]]
  (when-let [content (some-> file io/file slurp)]
    (let [line           row
          matching-line  (dec line)
          start-line     (max (- matching-line 4) 0)
          end-line       (+ matching-line 6)
          [before after] (->>
                           (str/split-lines content)
                           (map-indexed list)
                           (drop start-line)
                           (take (- end-line start-line))
                           (split-at (inc (- matching-line start-line))))
          snippet-lines  (concat before
                                 [[nil (str (str/join "" (repeat (dec col) " "))
                                            (str "^--- unused var"))]]
                                 after)
          indices        (map first snippet-lines)
          max-size       (reduce max 0 (map (comp count str) indices))
          snippet-lines  (map (fn [[idx line]]
                                (if idx
                                  (let [line-number (inc idx)]
                                    (str (format (str "%" max-size "d: ") line-number) line))
                                  (str (str/join (repeat (+ max-size 2) " ")) line)))
                              snippet-lines)]
      (str/join "\n" snippet-lines))))

(defn remove-locs [file zloc locs locs->syms
                   {:keys [:interactive
                           :dry-run
                           :silent]
                    :or   {interactive true}
                    :as   opts}]
  (loop [zloc          zloc
         locs          (seq locs)
         made-changes? false]
    (if locs
      (let [[row col :as loc] (first locs)
            node              (z/node zloc)
            m                 (meta node)]
        ;; (prn sym)
        (if (and (= row (:row m))
                 (= col (:col m)))
          (let [sym     (get locs->syms loc)
                context (if sym (node/string node)
                            ;; get surrounding lines if no sym found
                            (loc-context file loc))]
            (when-not silent
              (println "Found unused var:")
              (println "------------------")
              (println context)
              (println "------------------"))
            (let [remove? (cond dry-run false
                                interactive
                                (= "Y" (interact opts sym))
                                :else   true)
                  zloc    (if remove? (z/remove zloc) (z/next zloc))]
              (recur zloc (next locs) (or remove? made-changes?))))
          (recur (z/next zloc) locs made-changes?)))
      {:zloc          zloc
       :made-changes? made-changes?})))

(defn recursive? [{:keys [:from :from-var :to :name]}]
  (and (= from to)
       (= from-var name)))

(defn carve!
  "Removes unused vars from file."
  [file vs {:keys [:out-dir :silent] :as opts}]
  (try
    (let [zloc (z/of-file file)
          locs->syms (->> vs
                          (map (fn [{:keys [:row :col :ns :name]}]
                                 [[row col]
                                  (when (and ns name) ;; otherwise, nil sym
                                    (symbol (str ns) (str name)))]))
                          (into {}))
          locs (keys locs->syms)
          locs (sort locs)
          _ (when (and (not silent) (seq locs))
              (println "Carving" file)
              (println))
          {:keys [:made-changes? :zloc]}
          (remove-locs file zloc locs locs->syms opts)]
      (when made-changes?
        (let [file (io/file file)
              file (if (.isAbsolute file) file
                       (io/file out-dir file))]
          (io/make-parents file)
          (when-not silent (println "Writing result to" (.getCanonicalPath file)))
          (with-open [w (io/writer file)]
            (z/print-root zloc w)))))
    (catch Exception e
      (when-not silent
        (binding [*out* *err*]
          (println (str "Exception thrown when analyzing " file "."))
          (println e))))))

(defn ignore? [api-namespaces {:keys [:ns :export :defined-by :test :private :name]}]
  (or
    test
    export
    (when (contains? api-namespaces ns)
      (not private))
    (= (str name) "-main")
    (let [ns (namespace defined-by)
          nm (clojure.core/name defined-by)]
      (and (or (= "clojure.core" ns)
               (= "cljs.core" ns))
           (or (= "deftype" nm)
               (= "defrecord" nm)
               (= "defprotocol" nm)
               (= "definterface" nm))))))

(defn reportize [results]
  (sort-by (juxt :filename :row :col)
           (map #(select-keys % [:filename :row :col :ns :name])
                results)))

(defn print-report [report format]
  (case format
    :edn  (prn report)
    :text (doseq [{:keys [:filename :row :col :ns :name]} report]
            (println (str filename ":" row ":" col " " ns "/" name)))
    :ignore (doseq [{:keys [:ns :name]} report]
              (println (str ns "/" name)))
    (prn report)))

(defn analyze [opts paths]
  (let [{:keys [:clj-kondo/config]} opts
        result (clj-kondo/run!
                 {:lint   paths
                  :config (merge config {:output {:analysis true}})})
        unused-var-refers (->> result :findings
                               (filter #(= (:type %) :unused-referred-var)))
        {:keys [:var-definitions :var-usages]} (:analysis result)
        var-usages (remove recursive? var-usages)]
    {:var-definitions var-definitions
     :var-usages      var-usages
     :unused-var-refers unused-var-refers}))

(defn make-absolute-paths [dir paths]
  (mapv #(.getPath (io/file dir %)) paths))

(defn do-run! [opts]
  (let [{:keys [:carve-ignore-file
                :ignore-vars
                :paths
                :api-namespaces
                :aggressive
                :dry-run
                :out-dir] :as opts} (sanitize-opts opts)
        ignore (map (fn [ep]
                      [(symbol (namespace ep)) (symbol (name ep))])
                    ignore-vars)
        re-analyze? (not dry-run)]
    (loop [removed #{}
           results []
           analysis (analyze opts paths)]
      (let [{:keys [:var-definitions :var-usages :unused-var-refers]} analysis
            ;; the ignore file can change by interactively adding to it, so we
            ;; have to read it in each loop
            ignore-from-config (read-carve-ignore-file carve-ignore-file)
            ignore-from-config (map (fn [ep]
                                      [(symbol (namespace ep)) (symbol (name ep))])
                                    ignore-from-config)
            definitions-by-ns+name (index-by (juxt :ns :name) var-definitions)
            defined-vars (set (map (juxt :ns :name) var-definitions))
            defined-vars (set/difference defined-vars removed)
            ;; var usages contains the full set of usages as detected by
            ;; clj-kondo since we already removed some of the functions, not all
            ;; usage may be relevant anymore
            var-usages (remove (fn [usage]
                                 (let [from-var (:from-var usage)
                                      from-ns (:from usage)]
                                  (and from-var from-ns
                                       (contains? removed [from-ns from-var]))))
                               var-usages)
            used-vars (set (map (juxt :to :name) var-usages))
            used-vars (reduce into used-vars [ignore-from-config ignore])
            unused-vars (set/difference (set defined-vars) used-vars)
            unused-vars-data (map definitions-by-ns+name unused-vars)
            unused-vars-data (remove #(ignore? api-namespaces %) unused-vars-data)
            ;; update unused-vars with ignored ones (deftest, etc)
            unused-vars (set (map (juxt :ns :name) unused-vars-data))
            results (reduce into results [unused-vars-data unused-var-refers])]
        (if (or (seq unused-vars-data)
                (seq unused-var-refers))
          (do (when-not (:report opts)
                (let [data-by-file (->> unused-vars-data
                                        (concat unused-var-refers)
                                        (group-by :filename))]
                  (doseq [[file vs] data-by-file]
                    (carve! file vs opts))))
              (if aggressive
                (recur (into removed unused-vars)
                       results
                       (if re-analyze?
                         (analyze opts (make-absolute-paths out-dir paths))
                         ;; remove unused-var-refers to prevent looping forever
                         (dissoc analysis :unused-var-refers)))
                (reportize results)))
          (reportize results))))))

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
(s/def ::format #{:edn :text :ignore})
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

(defn load-opts
  "Load options, giving higher precedence to options passed from the CLI"
  [config opts]
  (let [opts (if (:merge-config opts)
               (if config (merge config opts)
                   opts)
               (or opts config))]
    (validate-opts! opts)
    opts))

(defn run+
  ([] (run+ nil))
  ([opts]
   (let [config-file (io/file ".carve/config.edn")
         config (when (.exists config-file)
                  (edn/read-string (slurp config-file)))
         opts (load-opts config opts)
         report (do-run! opts)]
     {:report report
      :config opts})))

(defn run!
  ([] (run! nil))
  ([opts]
   (:report (run+ opts))))
