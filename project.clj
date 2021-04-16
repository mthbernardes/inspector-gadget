(defproject inspector-gadget "0.1.0-SNAPSHOT"
  :description "Leiningen plugin responsible for finding possible vulnerabilities."
  :url "https://github.com/mthbernardes/inspector-gadget"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [borkdude/sci "0.2.4"]
                 [borkdude/edamame "0.0.11-alpha.28"]]
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :eval-in-leiningen true)