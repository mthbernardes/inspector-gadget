(ns inspector-gadget.rules.xxe
  (:require [inspector-gadget.logic.namespace :as namespace]
            [inspector-gadget.logic.function :as function]))

(defn detect [code]
  "Detect usage of vulnerable XML parser."
  (if-let [dependency (namespace/find-dependency-require code "clojure.xml")]
    (let [function (function/build-namespaced-fn-to-lookup dependency "parse")
          spec (function/build-fn-usage-spec function)
          findings (function/find-fn-usage code spec)]
      (assoc {} :dependency dependency
                :findings findings))))

(comment
  (require '[inspector-gadget.diplomat.file :as file])
  (def code "(ns banana (:require [clojure.xml :as x][clojure.java.shell :as shell]))
  (->> \"100\" x/parse)")
  (-> code java.io.StringReader. file/read-it detect))