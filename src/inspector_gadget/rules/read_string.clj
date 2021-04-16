(ns inspector-gadget.rules.read-string
  (:require [inspector-gadget.logic.function :as function]))

(defn detect [code]
  "Detect usage of vulnerable XML parser."
  (let [function (symbol "read-string")
        findings (function/find-fn-usage code function)]
    (when (seq findings)
      (assoc {} :dependency 'clojure.core
                :findings findings))))