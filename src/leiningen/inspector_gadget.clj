(ns leiningen.inspector-gadget
  (:require [inspector-gadget.core :as inspector-gadget]
            [leiningen.core.eval :as eval]
            [leiningen.core.project :as project]))

(defn- inspector-gadget-version [{:keys [plugins]}]
  (some (fn [[plugin-name version]]
          (when (= plugin-name 'inspector-gadget/inspector-gadget)
            version))
        plugins))

(defn- add-docstring-checker-dep [project]
  (project/merge-profiles
    project
    [{:dependencies [['inspector-gadget (inspector-gadget-version project)]]}]))

(defn inspector-gadget
  "I don't do a lot."
  [{:keys [source-paths] :as project} & rules-path]
  (println "Scanning code for vulnerabilities...")
  (eval/eval-in-project
    (add-docstring-checker-dep project)
    `(inspector-gadget/main ~(vec source-paths) ~@rules-path)
    '(require 'inspector-gadget.core)))