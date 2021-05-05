(ns inspector-gadget.adapter.regex
  (:require [clojure.spec.alpha :as s])
  (:import (java.util.regex Pattern)))

(defn ^:private read-rule [rule-regex]
  (binding [*read-eval* false]
    (read-string rule-regex)))

(defn ^:private rand-keyword []
  (->> (take 10 (repeatedly #(char (+ (rand 26) 65))))
       (apply str)
       keyword))

(defn ^:private regex? [value]
  (= Pattern (type value)))

(defn ^:private parse-rule-symbol [rule-symbol ns-fn]
  (cond
    (= rule-symbol '$) `(s/* any?)

    (= rule-symbol '$lookup-function) `(fn function-match# [arg#]
                                         (= arg# '~ns-fn))

    (= rule-symbol '$keyword) `(partial keyword?)

    (= rule-symbol '$regex) `(partial regex?)

    (= rule-symbol '$map) `(partial map?)

    (= rule-symbol '$vector) `(partial vector?)

    (= rule-symbol '$list) `(partial list?)

    (= rule-symbol '$string) `(partial string?)

    (= rule-symbol '$symbol) `(partial symbol?)

    (= rule-symbol '$number) `(partial number?)

    (= rule-symbol '$number) `(partial number?)

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
  (def spec (regex->spec "($lookup-function #\"sh|bash\" \"-c\" $)" 'shell/sh))
  (def nested-spec (regex->spec "(test ($lookup-function #\"sh|bash\" \"-c\" $))" 'shell/sh)) ; TODO: add support to nested expressions
  (s/valid? spec '(shell/sh "sh" "-c" "id")))