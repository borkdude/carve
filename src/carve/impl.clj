(ns carve.impl
  {:no-doc true}
  (:refer-clojure :exclude [run!])
  (:require
   [clj-kondo.core :as clj-kondo]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.set :as set]
   [rewrite-cljc.node :as node]
   [rewrite-cljc.zip :as z]))

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
                    :interactive (or interactive interactive?))
        opts (update opts :api-namespaces set)
        opts (update opts :carve-ignore-file
                     (fn [ci]
                       (if (nil? ci) ".carve_ignore"
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
    (when-not (.exists ignore-file) (.createNewFile ignore-file))
    (spit carve-ignore-file s :append true)))

(defn interact [{:keys [:carve-ignore-file]} sym]
  (println (format "Type Y to remove or i to add %s to %s" sym carve-ignore-file))
  (let [input (read-line)]
    (when (= "i" input)
      (add-to-carve-ignore-file carve-ignore-file (str sym "\n")))
    input))

(defn remove-locs [zloc locs locs->syms {:keys [:interactive
                                                :dry-run
                                                :silent]
                                         :or {interactive true}
                                         :as opts}]
  (loop [zloc zloc
         locs (seq locs)
         made-changes? false]
    (if locs
      (let [[row col :as loc] (first locs)
            node (z/node zloc)
            m (meta node)
            sym (get locs->syms loc)]
        ;; (prn sym)
        (if (and (= row (:row m))
                 (= col (:col m)))
          (do (when-not silent
                (println "Found unused var:")
                (println "------------------")
                (println (node/string node))
                (println "------------------"))
              (let [remove? (cond dry-run false
                                  interactive
                                  (= "Y" (interact opts sym))
                                  :else true)
                    zloc (if remove? (z/remove zloc) (z/next zloc))]
                (recur zloc (next locs) (or remove? made-changes?))))
          (recur (z/next zloc) locs made-changes?)))
      {:zloc zloc
       :made-changes? made-changes?})))

(defn recursive? [{:keys [:from :from-var :to :name]}]
  (and (= from to)
       (= from-var name)))

(defn carve!
  "Removes unused vars from file."
  [file vs {:keys [:out-dir :silent] :as opts}]
  (let [zloc (z/of-file file)
        locs->syms (into {}
                         (map (fn [{:keys [:row :col :ns :name]}]
                                [[row col] (symbol (str ns) (str name))]) vs))
        locs (keys locs->syms)
        locs (sort locs)
        _ (when (and (not silent) (seq locs))
            (println "Carving" file)
            (println))
        {:keys [:made-changes? :zloc]}
        (remove-locs zloc locs locs->syms opts)]
    (when made-changes?
      (let [file (io/file file)
            file (if (.isAbsolute file) file
                     (io/file out-dir file))]
        (io/make-parents file)
        (when-not silent (println "Writing result to" (.getCanonicalPath file)))
        (with-open [w (io/writer file)]
          (z/print-root zloc w))))))

(defn ignore? [api-namespaces {:keys [:ns :export :defined-by :test :private :name]}]
  (or
    test
    export
    (when (contains? api-namespaces ns)
      (not private))
    (= (str name) "-main")
    (= 'clojure.core/deftype defined-by)
    (= 'clojure.core/defrecord defined-by)
    (= 'clojure.core/defprotocol defined-by)
    (= 'clojure.core/definterface defined-by)))

(defn reportize [results]
  (sort-by (juxt :filename :row :col)
           (map #(select-keys % [:filename :row :col :ns :name])
                results)))

(defn print-report [report format]
  (case format
    :edn  (prn report)
    :text (doseq [{:keys [:filename :row :col :ns :name]} report]
            (println (str filename ":" row ":" col " " ns "/" name)))
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

(defn run! [opts]
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
            results (into results unused-vars-data)]
        (if (or (seq unused-vars-data) (seq unused-var-refers))
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
