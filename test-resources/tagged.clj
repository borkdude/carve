(ns tagged)

(defn normal-fn
  []
  42)

(defn also-ignore
  []
  21)

(defn ^:ignore-me to-ignore
  []
  (also-ignore))
