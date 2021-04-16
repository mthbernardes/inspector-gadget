(ns inspector-gadget.rules.xxe
  (:require [inspector-gadget.logic.namespace :as namespace]
            [inspector-gadget.logic.function :as function]))

(defn detect [code]
  "Detect usage of vulnerable XML parser."
  (if-let [dependency (namespace/find-dependency-require code "clojure.xml")]
    (let [function (function/build-namespaced-fn-to-lookup dependency "parse")
          findings (function/find-fn-usage code function)]
      (assoc {} :dependency dependency
                :findings findings))))