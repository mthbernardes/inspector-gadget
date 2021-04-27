(ns inspector-gadget.core
  (:require [clojure.pprint :as pprint]
            [inspector-gadget.diplomat.file :as file]
            [inspector-gadget.logic.parser :as parser]
            [clojure.java.io :as io]))

(def default-rules
  (let [files ["clojure-xml-xxe.edn" "read-string.edn" "shell-injection.edn"]]
    (->> files
         (map io/resource)
         (map slurp)
         (map clojure.edn/read-string))))

(defn execute-rule [code {:keys [checks] :as rule}]
  (let [checks-result (->> checks
                           (mapv #(parser/parse-rule-check code %))
                           (filter identity))]
    (when (seq checks-result)
      (assoc rule :findings checks-result))))

(defn scan [file rules]
  (println (str "Searching vulnerabilities on file: " (str file)))
  (let [filename (str file)
        code (file/read-it file)
        findings (->> rules
                      (mapv #(execute-rule code %))
                      (filter identity))]
    (when (seq findings)
      {:filename filename
       :findings findings})))

(defn main [source-paths & rules-path]
  (let [custom-rules (when rules-path (file/read-rules (first rules-path)))
        rules (concat default-rules custom-rules)
        files (->> source-paths
                   (map file/find-clojure-files)
                   (reduce concat))
        results (->> (mapv #(scan % rules) files)
                     (filter identity))]
    (when (seq results)
      (spit "vulnerabilities.edn" (with-out-str (pprint/pprint results)))
      (println "Findings saved on file vulnerabilities.edn"))))