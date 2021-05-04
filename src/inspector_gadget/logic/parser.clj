(ns inspector-gadget.logic.parser
  (:require [inspector-gadget.logic.function :as function]
            [inspector-gadget.logic.namespace :as namespace]
            [inspector-gadget.adapter.regex :as regex]))

(defmulti parse-rule-check (fn [_ {:keys [type]}] type))

(defmethod parse-rule-check :import-and-usage [code {:keys [ns-name function-name type]}]
  (if-let [dependency (namespace/find-dependency-require code ns-name)]
    (let [function (function/build-namespaced-fn-to-lookup dependency function-name)
          spec (function/build-fn-usage-spec function)
          findings (function/find-fn-usage code spec)]
      (when findings
        (assoc {} :dependency dependency
                  :findings findings
                  :type type)))))

(defmethod parse-rule-check :usage [code {:keys [ns-name function-name type]}]
  (let [function (symbol function-name)
        spec (function/build-fn-usage-spec function)
        findings (function/find-fn-usage code spec)]
    (when (seq findings)
      (assoc {} :dependency ns-name
                :findings findings
                :type type))))

(defmethod parse-rule-check :import-and-usage-custom-spec [code {:keys [ns-name function-name type fn-regex]}]
  (if-let [dependency (namespace/find-dependency-require code ns-name)]
    (let [function (function/build-namespaced-fn-to-lookup dependency function-name)
          spec (regex/regex->spec fn-regex function)
          findings (function/find-fn-usage code spec)]
      (when findings
        (assoc {} :dependency dependency
                  :findings findings
                  :type type)))))

(defmethod parse-rule-check :default [_ _]
  :not-implemented)

(comment
  (def code ['(ns test
                (:require [clojure.java.shell :as shell]))
             '(shell/sh "bash" "-c" (str "ls " test))])
  (def rule {:type          :test
             :ns-name       "clojure.java.shell"
             :function-name "sh"
             :fn-regex      "(_ %fn% #\"sh|bash\" \"-c\" _)"})
  (parse-rule-check code rule))