(ns inspector-gadget.adapter.regex
  (:require [clojure.spec.alpha :as s])
  (:import (java.util.regex Pattern)))

(defn tap [x] (println x) x)

(defn ^:private read-rule [rule-regex]
  (binding [*read-eval* false]
    (read-string rule-regex)))

(defn ^:private rand-keyword []
  (->> (take 10 (repeatedly #(char (+ (rand 26) 65))))
       (apply str )
       keyword))

(defn ^:private regex? [value]
  (= Pattern (type value)))

(defn ^:private parse-rule-symbol [rule-symbol ns-fn]
  (cond
    (= rule-symbol '_) `(s/* any?)

    (= rule-symbol '%fn%) `(fn function-match# [arg#]
                             (= arg# '~ns-fn))

    (regex? rule-symbol) `(fn regex-match# [arg#]
                            (->> arg#
                                 (re-matches ~rule-symbol)
                                 nil?
                                 not))

    (string? rule-symbol) `(fn string-match# [arg#]
                             (= arg# ~rule-symbol))))

(defn ^:private parse-rule [regex-rule ns-fn]
  (reduce (fn [spec rule-symbol]
            (let [index (rand-keyword)]
              (->> [(parse-rule-symbol rule-symbol ns-fn) index]
                   (into '())
                   (concat spec))))
          `(s/cat) regex-rule))

(defn regex->spec [rule-regex namespaced-fn]
  (-> rule-regex
      read-rule
      (parse-rule namespaced-fn)
      eval))

(comment
  (def spec (regex->spec "(%fn% #\"sh|bash\" \"-c\" _)" 'shell/sh))
  (s/valid? spec '(shell/sh "sh" "-c" "id")))