(ns inspector-gadget.rules.shell-injection
  (:require [inspector-gadget.logic.namespace :as namespace]
            [inspector-gadget.logic.function :as function]
            [clojure.spec.alpha :as s]
            [inspector-gadget.diplomat.file :as file]))

(defn build-fn-usage-spec [namespaced-fn]
  (s/* (s/cat :before (s/* any?)
              :fn-name #(= namespaced-fn %)
              :bash-args #(not (nil? (re-matches #"sh|bash" %)))
              :bash-c-arg #(= "-c" %)
              :args (s/* any?))))

(defn detect [code]
  "Detect usage of bash -c on clojure.java.shell/sh invoke."
  (if-let [dependency (namespace/find-dependency-require code "clojure.java.shell")]
    (let [function (function/build-namespaced-fn-to-lookup dependency "sh")
          spec (build-fn-usage-spec function)
          findings (function/find-fn-usage code spec)]
      (assoc {} :dependency dependency
                :findings findings))))

(comment
  (require '[inspector-gadget.diplomat.file :as file])
  (def code "(ns banana (:require [clojure.java.shell :as shell]))
  (->> \"id\" (shell/sh \"bash\" \"-c\"))")
  (-> code java.io.StringReader. file/read-it detect))