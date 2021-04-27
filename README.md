# inspector-gadget

A Leiningen plugin responsible for finding possible vulnerabilities. Totally inspired on borkdude's [grasp](https://github.com/borkdude/grasp).

# Rules

## Default rules
- [clojure-xml-xxe](resources/clojure-xml-xxe.edn)
- [read-string](resources/read-string.edn)
- [shell-injection](resources/shell-injection.edn)

## Custom Rules
You can define your own set of rules just by writting a edn like the following one.
```clojure
{:name        :shell-injection
 :description "Detect usage of bash -c on clojure.java.shell/sh invoke."
 :checks      [{:type                  :import-and-usage
                :ns-name               "clojure.java.shell"
                :function-name         "sh"
                :function-spec-builder (fn [namespaced-fn]
                                         (s/* (s/cat :before (s/* any?)
                                                :fn-name (fn [symbol] (= namespaced-fn symbol))
                                                :bash-args (fn [bash-arg]
                                                             (->> bash-arg
                                                               (re-matches (re-pattern "sh|bash"))
                                                               nil?
                                                               not))
                                                :bash-c-arg (fn [bash-c-arg]
                                                              (= "-c" bash-c-arg))
                                                :args (s/* any?))))}]}
```

Which one of them needs to specify the rule name, a description and the steps to check if the code is vulnerable.
There are two types of checks.

- **import-and-usage** which lookup for the usage of a required function.
- **usage** which lookup for the usage of non required function.

Both of them needs to define in the checks a namespace, function name and optionally a fn which returns a spec representing the function usage.

in order to use your custom rules just save then as an edn file on a directory and specify it on the plugin usage.

# Usage

Use this for user-level plugins:

Put `[inspector-gadget "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your `:user`
profile.

Use this for project-level plugins:

Put `[inspector-gadget "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your project.clj.

Execute it on your project directory.
`$ lein inspector-gadget`

or with a custom directory for rules.
`$ lein inspector-gadget /tmp/rules/`

# Example result
```clojure
({:filename "/home/user/dev/projects/test/src/shit/vulnerable-code.clj",
  :findings
  ({:name :clojure-xml-xxe,
    :description
    "Detect usage of vulnerable XML parser on clojure.xml.",
    :checks
    [{:type :import-and-usage,
      :ns-name "clojure.xml",
      :function-name "parse"}],
    :findings
    ({:dependency {:alias "xml", :code [clojure.xml :as xml]},
      :findings
      [{:line 16, :column 7, :code (xml/parse istream)}
       {:line 21,
        :column 1,
        :code (-> "/home/user/image.svg" slurp xml/parse)}],
      :type :import-and-usage})}
   {:name :read-string,
    :description "Detect usage of vulnerable read-string function.",
    :checks
    [{:type :usage,
      :ns-name "clojure.core",
      :function-name "read-string"}],
    :findings
    ({:dependency "clojure.core",
      :findings
      [{:line 8, :column 3, :code (read-string "a")}
       {:line 9, :column 3, :code (-> b read-string (+ 1))}],
      :type :usage})}
   {:name :shell-injection,
    :description
    "Detect usage of bash -c on clojure.java.shell/sh invoke.",
    :checks
    [{:type :import-and-usage,
      :ns-name "clojure.java.shell",
      :function-name "sh",
      :function-spec-builder
      (fn
        [namespaced-fn]
        (s/*
          (s/cat
            :before
            (s/* any?)
            :fn-name
            (fn [symbol] (= namespaced-fn symbol))
            :bash-args
            (fn
              [bash-arg]
              (->> bash-arg (re-matches (re-pattern "sh|bash")) nil? not))
            :bash-c-arg
            (fn [bash-c-arg] (= "-c" bash-c-arg))
            :args
            (s/* any?))))}],
    :findings
    ({:dependency
      {:alias "shell", :code [clojure.java.shell :as shell]},
      :findings [{:line 25, :column 8, :code (shell/sh "bash" "-c")}],
      :type :import-and-usage})})})
```
