(ns inspector-gadget.rules.read-string
  (:require [inspector-gadget.logic.function :as function]))

(defn detect [code]
  "Detect usage of vulnerable read-string function."
  (let [function (symbol "read-string")
        spec (function/build-fn-usage-spec function)
        findings (function/find-fn-usage code spec)]
    (when (seq findings)
      (assoc {} :dependency 'clojure.core
             :findings findings))))
(comment
  (require '[inspector-gadget.diplomat.file :as file])
  (def code "(ns banana (:require [clojure.java.shell :as shell]))
  (->> \"id\" (shell/sh \"bash\" \"-c\") read-string)")
  (-> code java.io.StringReader. file/read-it detect))