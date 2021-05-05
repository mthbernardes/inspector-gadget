(ns inspector-gadget.logic.namespace
  (:require [clojure.spec.alpha :as s]))

(s/def ::namespace
  (s/cat :ns-symbol #(= 'ns %)
         :namespace symbol?
         :body (s/* any?)))

(defn ^:private find-namespace [code]
  (->> code
       (filter #(s/valid? ::namespace %))
       last))

(defn ^:private find-dependencies [form]
  (and (list? form)
       (= :require (first form))))

(defn ^:private build-require-spec [fn-namespace]
  (let [fn-namespace (symbol fn-namespace)]
    (s/cat :namespace #(= fn-namespace %)
           :as (s/? keyword?)
           :symbol (s/? (s/or :refer (s/tuple symbol?)
                              :alias symbol?)))))

(defn ^:private parse-require [require]
  (let [require-metadata (meta require)
        alias-or-refer (cond
                         (= (second require) :as) (assoc require-metadata :alias (-> require last str))
                         (= (second require) :refer) (assoc require-metadata :refer (-> require last))
                         :else (assoc require-metadata :alias (-> require first str)))]
    (assoc alias-or-refer :code require)))

(defn find-dependency-require [code namespace]
  (let [spec (build-require-spec namespace)
        ns-code (find-namespace code)
        dependencies (->> ns-code
                          (filter find-dependencies)
                          last
                          rest)]
    (some->> dependencies
             (filter #(s/valid? spec %))
             last
             parse-require)))