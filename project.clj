(defproject org.clojars.mthbernardes/inspector-gadget "1.0.0"
  :description "Leiningen plugin responsible for finding possible vulnerabilities."
  :url "https://github.com/mthbernardes/inspector-gadget"

  :deploy-repositories [["clojars" {:url "https://repo.clojars.org"
                                    :username :env/CLOJARS_USERNAME
                                    :password :env/CLOJARS_PASSWORD}]]

  :plugins [[lein-cljfmt "0.6.1"]
            [jonase/eastwood "0.3.5"]
            [lein-nsorg "0.2.0"]]

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/data.json "2.2.2"]
                 [borkdude/sci "0.2.4"]
                 [borkdude/edamame "0.0.11-alpha.28"]]

  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}

  :aliases {"lint"                                ["do" ["cljfmt" "check"] ["nsorg"] ["eastwood" "{:namespaces [:source-paths]}"]]
            "lint-fix"                            ["do" ["cljfmt" "fix"] ["nsorg" "--replace"]]}

  :eval-in-leiningen true)
