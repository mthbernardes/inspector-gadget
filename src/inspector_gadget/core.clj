(ns inspector-gadget.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [inspector-gadget.diplomat.file :as file]
            [inspector-gadget.logic.sarif :as sarif]
            [inspector-gadget.logic.parser :as parser]))

(defn- mapv-filter
  ([f coll]
   (mapv-filter f identity coll))
  ([f filter-fn coll]
   (filter filter-fn (mapv f coll))))

(def default-rules
  (let [files ["clojure-xml-xxe.edn" "read-string.edn" "shell-injection.edn"]]
    (->> files
         (map io/resource)
         (map slurp)
         (map edn/read-string))))

(defn execute-rule [filename code {:keys [sarif-definition] :as rule} results]
  (let [check-result (parser/parse-rule-check filename code rule)]
    (when (seq check-result)
      (let [sarif-result (sarif/build-result filename check-result sarif-definition)]
        (conj results sarif-result)))))

(defn scan [file rules]
  (println (str "Searching vulnerabilities on file: " (str file)))
  (let [code (file/read-it file)
        results (reduce #(execute-rule file code %2 %1) [] rules)]
    (when (seq results)
      results)))

(defn main [source-paths & rules-path]
  (let [rules (->> (when rules-path (file/read-rules (first rules-path)))
                   (concat default-rules))
        files (mapcat file/find-clojure-files source-paths)
        results (mapv-filter #(scan % rules) files)
        sarif-run (sarif/build-sarif-run rules)]
    (when (seq results)
      (->> results
           (assoc sarif-run :results )
           (sarif/build-sarif-report)
           json/write-str
           (spit "result.sarif"))
      (println "Findings saved on file result.sarif"))))