(ns inspector-gadget.logic.function
  (:require [clojure.spec.alpha :as s]))

(defn ^:private build-fn-usage-spec [namespaced-fn]
  (s/* (s/cat :before (s/* any?)
              :fn-name #(= namespaced-fn %)
              :args (s/* any?))))

(defn find-fn-usage [code namespaced-function]
  (let [fn-spec (build-fn-usage-spec namespaced-function)
        findings (some->> code
                          (tree-seq coll? identity)
                          (filter #(s/valid? fn-spec %))
                          (filter seq))]
    (if (seq findings)
      (mapv #(assoc (meta %) :code %) findings))))

(defn build-namespaced-fn-to-lookup [dependency function]
  (cond
    (string? (:alias dependency)) (symbol (:alias dependency) function)
    (and (vector? (:refer dependency))
         (some #(= (symbol function) %) (:refer dependency))) (symbol function)
    :else nil))