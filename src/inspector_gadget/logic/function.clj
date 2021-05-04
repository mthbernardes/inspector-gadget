(ns inspector-gadget.logic.function
  (:require [clojure.spec.alpha :as s]))

(defn find-fn-usage [code spec]
  (let [findings (some->> code
                          (tree-seq coll? identity)
                          (filter #(s/valid? spec %))
                          (filter seq))]
    (if (seq findings)
      (mapv #(meta %) findings))))

(defn build-namespaced-fn-to-lookup [dependency function]
  (cond
    (string? (:alias dependency)) (symbol (:alias dependency) function)
    (and (vector? (:refer dependency))
         (some #(= (symbol function) %) (:refer dependency))) (symbol function)
    :else nil))