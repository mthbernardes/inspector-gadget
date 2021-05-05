(ns inspector-gadget.logic.parser
  (:require [inspector-gadget.adapter.regex :as regex]
            [inspector-gadget.logic.function :as function]
            [inspector-gadget.logic.namespace :as namespace]))

(defmulti parse-rule-check (fn [_ {{:keys [type]} :check}] (identity type)))

(defmethod parse-rule-check :usage [code {{:keys [function-name fn-regex]} :check}]
  (let [function (symbol function-name)
        spec (regex/regex->spec fn-regex function)
        findings (function/find-fn-usage code spec)]
    (when (seq findings)
      findings)))

(defmethod parse-rule-check :import-and-usage [code {{:keys [ns-name function-name fn-regex]} :check}]
  (if-let [dependency (namespace/find-dependency-require code ns-name)]
    (let [function (function/build-namespaced-fn-to-lookup dependency function-name)
          spec (regex/regex->spec fn-regex function)
          findings (function/find-fn-usage code spec)]
      (when findings
        findings))))

(defmethod parse-rule-check :default [_ _]
  :not-implemented)

(comment
  (def code ['(ns test
                (:require [clojure.java.shell :as shell]))
             '(read-string "1")
             '(shell/sh "bash" "-c" (str "ls " test))])

  (def rule {:sarif-definition {:id               :shell-injection
                                :name             "Shell injection"
                                :shortDescription {:text "Detect usage of bash -c on clojure.java.shell/sh invoke."}
                                :fullDescription  {:text "Detect usage of bash -c on clojure.java.shell/sh invoke."}
                                :help             {:text "Detect usage of bash -c on clojure.java.shell/sh invoke."}
                                :properties       {:precision :medium}}
             :check           {:type          :usage
                               :ns-name       "clojure.core"
                               :function-name "read-string"
                               :fn-regex      "($ $lookup-function $)"}})
  (parse-rule-check (java.io.File. "/home/dpr/test/src/index.clj") code rule))