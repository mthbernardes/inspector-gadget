(ns inspector-gadget.logic.parser
  (:require [inspector-gadget.logic.function :as function]
            [inspector-gadget.logic.namespace :as namespace]))

(defmulti parse-rule-check (fn [_ {:keys [type]}] type))

(defmethod parse-rule-check :import-and-usage [code {:keys [ns-name function-name spec-fn type]}]
  (if-let [dependency (namespace/find-dependency-require code ns-name)]
    (let [function (function/build-namespaced-fn-to-lookup dependency function-name)
          build-fn-usage-spec (or spec-fn function/build-fn-usage-spec)
          spec (build-fn-usage-spec function)
          findings (function/find-fn-usage code spec)]
      (when findings
        (assoc {} :dependency dependency
                  :findings findings
                  :type type)))))

(defmethod parse-rule-check :usage [code {:keys [ns-name function-name type spec-fn]}]
  (let [function (symbol function-name)
        build-fn-usage-spec (or spec-fn function/build-fn-usage-spec)
        spec (build-fn-usage-spec function)
        findings (function/find-fn-usage code spec)]
    (when (seq findings)
      (assoc {} :dependency ns-name
             :findings findings
             :type type))))

(defmethod parse-rule-check :default [_ _]
  :not-implemented)

