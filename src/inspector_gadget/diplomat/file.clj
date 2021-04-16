(ns inspector-gadget.diplomat.file
  (:require [sci.core :as sci]
            [edamame.core :as edamame]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(defn ^:private init []
  (sci/init {:load-fn  (fn [_] "")
             :readers  (fn [_x]
                         identity)
             :features #{:clj :cljs}}))

(defn read-it [file]
  (let [reader (-> file slurp edamame/source-reader)]
    (loop [init (init)
           form (sci/parse-next init reader)
           all-forms []]
      (if (= :sci.core/eof form)
        all-forms
        (let [next-form (sci/parse-next init reader)]
          (recur init
                 next-form
                 (conj all-forms form)))))))

(defn find-clojure-files [path]
  (->> path
       io/file
       file-seq
       (filter (fn [file]
                 (string/ends-with? (str file) ".clj")))))