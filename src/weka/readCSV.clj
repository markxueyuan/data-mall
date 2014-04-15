(ns weka.readCSV
  (:import [weka.core.converters ArffLoader CSVLoader]
           [java.io File]))

(defn ->options
  [& opts]
  (into-array String (map str (flatten (remove nil? opts)))))

(defn load-csv
  ([filename & {:keys [header] :or {header true}}]
   (let [options (->options (when-not header "-H"))
         loader (doto (CSVLoader.)
                  (.setOptions options)
                  (.setSource (File. filename)))]
     (.getDataSet loader))))

(def data (load-csv "D:/data/all_160.P3.csv" :header true))

;(.numAttributes data)


(defn load-arff
  ([filename]
   (.getDataSet (doto (ArffLoader.)
                  (.setFile) (File. filename)))))


(when-not false 3)

