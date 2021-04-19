# inspector-gadget

A Leiningen plugin responsible for finding possible vulnerabilities. Totally inspired on borkdude's [grasp](https://github.com/borkdude/grasp)

## Usage

Use this for user-level plugins:

Put `[inspector-gadget "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your `:user`
profile.

Use this for project-level plugins:

Put `[inspector-gadget "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your project.clj.

Execute it on your project directory.
`$ lein inspector-gadget`

## Vulnerabilities
- XXE using clojure.xml/parse
- RCE using clojure.core/read-string
- RCE using "bash -c" on clojure.java.shell/sh

### Example result
```clojure
({:filename "/home/user/dev/projects/example/src/example/teste.clj",
  :findings
  {:xxe
   {:dependency {:alias "xml", :code [clojure.xml :as xml]},
    :findings
    [{:line 24, :column 44, :code (xml/parse non-validating)}]},
   :shell-injection nil,
   :read-string
   {:dependency clojure.core,
    :findings
    [{:code [read-string]}
     {:line 26, :column 1, :code (-> read-string "1")}
     {:line 27, :column 1, :code (read-string "#=(inc 1)")}]}}}
 {:filename "/home/user/dev/projects/example/src/example/xxe.clj",
  :findings
  {:xxe
   {:dependency {:alias "xml", :code [clojure.xml :as xml]},
    :findings
    [{:line 16, :column 7, :code (xml/parse istream)}
     {:line 21,
      :column 1,
      :code (-> "/home/user/image.svg" slurp xml/parse)}]},
   :shell-injection
   {:dependency {:alias "shell", :code [clojure.java.shell :as shell]},
    :findings [{:line 25, :column 8, :code (shell/sh "bash" "-c")}]},
   :read-string
   {:dependency clojure.core,
    :findings
    [{:line 8, :column 3, :code (read-string "a")}
     {:line 9, :column 3, :code (-> b read-string (+ 1))}]}}}
 {:filename "/home/user/dev/projects/example/src/example/reescreve.clj",
  :findings
  {:xxe nil,
   :shell-injection nil,
   :read-string
   {:dependency clojure.core,
    :findings [{:line 43, :column 55, :code 'read-string}]}}}
 {:filename "/home/user/dev/projects/example/src/example/config.clj",
  :findings
  {:xxe
   {:dependency {:alias "xml", :code [clojure.xml :as xml]},
    :findings nil},
   :shell-injection nil,
   :read-string nil}})
```
