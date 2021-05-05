(ns inspector-gadget.logic.sarif)

(defn ^:praivate build-sarif-rules [rules rule]
  (let [sarif-definition (:sarif-definition rule)]
    (conj rules sarif-definition)))

(defn ^:private build-location [uri locations {:keys [line column]}]
  (let [location {:physicalLocation {:artifactLocation {:uri uri}
                                     :region           {:startLine  line
                                                        :startColumn column}}}]
    (conj locations location)))

(defn build-sarif-run [rules]
  {:tool {:driver {:name           "inspector-gadget"
                   :informationUri "https://github.com/mthbernardes/inspector-gadget"
                   :rules          (reduce build-sarif-rules [] rules)}}})

(defn build-result [filename findings {:keys [id shortDescription]}]
  (let [uri (->> filename .getPath (format "file://%s"))
        locations (reduce #(build-location uri %1 %2)
                          [] findings)]
    {:ruleId    id
     :message   shortDescription
     :locations locations}))

(defn build-sarif-report [run]
  {:$schema "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json"
   :version  "2.1.0"
   :runs [run]})