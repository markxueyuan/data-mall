(ns data-mall.readXML)

(use 'clojure.repl
     'clojure.java.javadoc)

(require '[incanter.core :as incanter]
         '[clojure.xml :as xml]
         '[clojure.zip :exclude [next replace remove] :as zip])

(defn load-xml-data
  [xml-file first-data next-data]
  (let [data-map (fn [node] [(:tag node) (first (:content node))])]
    (->>
     ;; 1. Parse the XML data file;
     (xml/parse xml-file)
     zip/xml-zip
     ;; 2. Walk it to extract the data nodes;
     first-data
     (iterate next-data)
     (take-while #(not (nil? %)))
     (map zip/children)
     ;; 3. Convert them into a sequence of maps;
     (map #(mapcat data-map %))
     (map #(apply array-map %))
     ;; 4. Convert into Incanter dataset
     incanter/to-dataset
     )))

(load-xml-data  "D:/data/small-sample.xml" zip/down zip/right)

;;;parse an xml file

(def x (xml/parse "D:/data/small-sample.xml"))
x
(xml/tag x)
(xml/attrs x)
(xml/content x)
(first (xml/content x))

;; recover xml
(xml/emit x)

;;; Use xml-seq

(xml-seq x)

(for [node (xml-seq x) :when (= :given-name (:tag node))]
  (first (:content node)))

;;; use xml zipper

(->> x
     zip/xml-zip)


















