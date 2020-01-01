(ns carve.impl
  {:no-doc true}
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [rewrite-cljc.node :as node]
   [rewrite-cljc.zip :as z]))

(defn index-by
  [f coll]
  (persistent! (reduce #(assoc! %1 (f %2) %2) (transient {}) coll)))

(defn read-carve-ignore-file [carve-ignore-file]
  (let [ignore-file (io/file carve-ignore-file)]
    (when (.exists ignore-file)
      (edn/read-string (format "[%s]" (slurp carve-ignore-file))))))

(defn add-to-carve-ignore-file [carve-ignore-file s]
  (let [ignore-file (io/file carve-ignore-file)]
    (when-not (.exists ignore-file) (.createNewFile ignore-file))
    (spit carve-ignore-file s :append true)))

(defn interactive [{:keys [:carve-ignore-file]} sym]
  (println (format "Type Y to remove or i to add %s to %s" sym carve-ignore-file))
  (let [input (read-line)]
    (when (= "i" input)
      (add-to-carve-ignore-file carve-ignore-file (str sym "\n")))
    input))

(defn remove-locs [zloc locs locs->syms {:keys [:interactive?
                                                :dry-run?]
                                         :or {interactive? true}
                                         :as opts}]
  (loop [zloc zloc
         locs (seq locs)
         made-changes? false]
    (if locs
      (let [[row col :as loc] (first locs)
            node (z/node zloc)
            m (meta node)]
        (if (and (= row (:row m))
                 (= col (:col m)))
          (do (println "Found unused var:")
              (println "------------------")
              (println (node/string node))
              (println "------------------")
              (let [remove? (cond dry-run? false
                                  interactive?
                                  (= "Y" (interactive opts (get locs->syms loc)))
                                  :else true)
                    zloc (if remove? (z/remove zloc) (z/next zloc))]
                (recur zloc (next locs) (or remove? made-changes?))))
          (recur (z/next zloc) locs made-changes?)))
      {:zloc zloc
       :made-changes? made-changes?})))

(defn ignore-by-definer? [defined-by]
  (or (= 'clojure.core/deftype defined-by)
      (= 'clojure.core/defrecord defined-by)
      (= 'clojure.core/definterface defined-by)
      (= 'clojure.core/defprotocol defined-by)))

(defn carve [file vs {:keys [:out-dir :api-namespaces]
                      :as opts}]
  (let [zloc (z/of-file file)
        locs->syms (into {}
                         (keep (fn [{:keys [:row :col :ns :name :private :test
                                            :defined-by]}]
                                 (when (and (not test)
                                            (not (ignore-by-definer? defined-by))
                                            (or (not (contains? api-namespaces ns))
                                                private))
                                   [[row col] (symbol (str ns) (str name))])) vs))
        locs (keys locs->syms)
        locs (sort locs)
        _ (when (seq locs)
            (println "Carving" file)
            (println))
        {:keys [:made-changes? :zloc]}
        (remove-locs zloc locs locs->syms opts)]
    (when made-changes?
      (let [out-file (io/file out-dir file)
            out-file (.getCanonicalFile out-file)]
        (io/make-parents out-file)
        (println "Writing result to" (.getPath out-file))
        (with-open [w (io/writer out-file)]
          (z/print-root zloc w))))))
