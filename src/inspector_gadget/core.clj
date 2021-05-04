(ns inspector-gadget.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [inspector-gadget.diplomat.file :as file]
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

(defn execute-rule [code {:keys [checks] :as rule}]
  (let [checks-result (mapv-filter #(parser/parse-rule-check code %) checks)]
    (when (seq checks-result)
      (assoc rule :findings checks-result))))

(defn scan [file rules]
  (println (str "Searching vulnerabilities on file: " (str file)))
  (let [code (file/read-it file)
        findings (mapv-filter #(execute-rule code %) rules)]
    (when (seq findings)
      {:filename (str file)
       :findings findings})))

(defn main [source-paths & rules-path]
  (let [rules (->> (when rules-path (file/read-rules (first rules-path)))
                   (concat default-rules))
        files (mapcat file/find-clojure-files source-paths)
        results (mapv-filter #(scan % rules) files)]
    (when (seq results)
      (spit "vulnerabilities.edn" (with-out-str (pprint/pprint results)))
      (println "Findings saved on file vulnerabilities.edn"))))