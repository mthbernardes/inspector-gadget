(ns inspector-gadget.core
  (:require [inspector-gadget.diplomat.file :as file]
            [inspector-gadget.rules.read-string :as rule.read-string]
            [inspector-gadget.rules.shell-injection :as rule.shell-injection]
            [inspector-gadget.rules.xxe :as rule.xxe]))

(defn execute-rules [file]
  (println (str "Searching vulnerabilities on file: " (str file)))
  (let [filename (str file)
        code (file/read-it file)
        xxe-result (rule.xxe/detect code)
        read-string-result (rule.read-string/detect code)
        shell-injection-result (rule.shell-injection/detect code)
        result (some #(not (nil? %)) [read-string-result xxe-result shell-injection-result])]
    (when result
      {:filename filename
       :findings {:xxe         xxe-result
                  :shell-injection shell-injection-result
                  :read-string read-string-result}})))

(defn main [source-paths]
  (let [files (->> source-paths
                   (map file/find-clojure-files)
                   (reduce concat))]
    (->> (mapv execute-rules files)
         (filter identity))))