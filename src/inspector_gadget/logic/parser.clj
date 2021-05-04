(ns inspector-gadget.logic.parser
  (:require [inspector-gadget.logic.function :as function]
            [inspector-gadget.logic.namespace :as namespace]
            [inspector-gadget.logic.sarif :as sarif]
            [inspector-gadget.adapter.regex :as regex]))

(defmulti parse-rule-check (fn [_ _ {{:keys [type]} :check}] (identity type)))

(defmethod parse-rule-check :usage [filename code {:keys [sarif-definition]
                                                   {:keys [function-name fn-regex]} :check}]
  (let [function (symbol function-name)
        spec (regex/regex->spec fn-regex function)
        findings (function/find-fn-usage code spec)]
    (when (seq findings)
      findings
      #_(sarif/build-result filename findings sarif-definition))))

(defmethod parse-rule-check :import-and-usage [filename code {:keys [sarif-definition]
                                                              {:keys [ns-name function-name fn-regex]} :check}]
  (if-let [dependency (namespace/find-dependency-require code ns-name)]
    (let [function (function/build-namespaced-fn-to-lookup dependency function-name)
          spec (regex/regex->spec fn-regex function)
          findings (function/find-fn-usage code spec)]
      (when findings
        findings
        #_(sarif/build-result filename findings sarif-definition)))))

(defmethod parse-rule-check :default [_ rule]
  (println rule)
  :not-implemented)

(comment
  (def code ['(ns test
                (:require [clojure.java.shell :as shell]))
             '(shell/sh "bash" "-c" (str "ls " test))])

  (def rule {:sarif-definition {:id               :shell-injection
                                :name             "Shell injection"
                                :shortDescription {:text "Detect usage of bash -c on clojure.java.shell/sh invoke."}
                                :fullDescription  {:text "Detect usage of bash -c on clojure.java.shell/sh invoke."}
                                :help             {:text "Detect usage of bash -c on clojure.java.shell/sh invoke."}
                                :properties       {:precision :medium}}
             :check           {:type          :import-and-usage
                               :ns-name       "clojure.java.shell"
                               :function-name "sh"
                               :fn-regex      "($ $lookup-function #\"sh|bash\" \"-c\" $)"}})
  (parse-rule-check (java.io.File. "/home/dpr/test/src/index.clj") code rule))