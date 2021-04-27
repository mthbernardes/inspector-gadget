(ns inspector-gadget.diplomat.file
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [edamame.core :as edamame]
            [sci.core :as sci]))

(defn ^:private find-rules [path]
  (->> path
       io/file
       file-seq
       (map #(.getPath %))
       (filter (fn [file]
                 (string/ends-with? (str file) ".edn")))))

(defn ^:private init []
  (sci/init {:load-fn  (fn [_] "")
             :readers  (fn [_x]
                         identity)
             :features #{:clj}}))

(defn read-it [file]
  (try
    (let [reader (-> file slurp edamame/source-reader)]
      (loop [init (init)
             form (sci/parse-next init reader)
             all-forms []]
        (if (= :sci.core/eof form)
          all-forms
          (let [next-form (sci/parse-next init reader)]
            (recur init
                   next-form
                   (conj all-forms form))))))
    (catch Exception e
      (println (format "Unable to read file %s. %s" (str file) (ex-message e))))))

(defn find-clojure-files [path]
  (->> path
       io/file
       file-seq
       (filter (fn [file]
                 (string/ends-with? (str file) ".clj")))))

(defn read-rule [path]
  (->> path
       slurp
       edn/read-string))

(defn read-rules [path]
  (->> path
       find-rules
       (mapv read-rule)))