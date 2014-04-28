(ns weka.column
  (:import [weka.filters Filter]
           [weka.filters.unsupervised.attribute Remove])
  (:require [clojure.string :as s]
            [weka.readCSV :as r]))

;renaming columns

(defn set-fields
  ([instances field-seq]
   (doseq [n (range (.numAttributes instances))]
     (.renameAttribute instances
                       (.attribute instances n)
                       (name (nth field-seq n))))))

(map #(.. r/data (attribute %) name)
     (range (.numAttributes r/data)))

#_(set-fields r/data
           [:geoid :sumlev :state :county :cbsa :csa :necta :cnecta :name
            :pop100 :housing-units-100 :pop100-2000 :housing-units-100-2000
            :race-total :race-total-2000 :race-white :race-white-2000
            :race-black :race-black-2000 :race-indian :race-indian-2000
            :race-asian :race-asian-2000 :race-hawaiian :race-hawaiian-2000
            :race-other :race-other-2000 :race-two-more :race-two-more-2000])

;removing columns

(defn attr-n
  [instances attr-name]
  (->> instances
       (.numAttributes)
       range
       (map #(vector % (.. instances (attribute %) name)))
       (filter #(= (second %) (name attr-name)))
       ffirst))

;(attr-n r/data :SUMLEV)

(defn delete-attrs
  [instances attr-names]
  (reduce (fn [ins na]
            (.deleteAttributeAt ins (attr-n ins na))
            ins)
          instances
          attr-names))


;(delete-attrs r/data [:sumlev :county :cbsa :csa :necta :cnecta])

;hiding columns

(defn filter-attributes
  ([dataset remove-attrs]
   (let [attrs (map inc (map (partial attr-n dataset) remove-attrs))
         options (r/->options "-R" (s/join \, (map str attrs)))
         rm (doto (Remove.)
              (.setOptions options)
              (.setInputFormat dataset))]
     (Filter/useFilter dataset rm))))

#_(def data-2010
  (filter-attributes r/data
                     [:pop100-2000  :housing-units-100-2000
                      :race-total-2000 :race-white-2000
                      :race-black-2000 :race-indian-2000
                      :race-asian-2000 :race-hawaiian-2000
                      :race-other-2000 :race-two-more-2000]))

#_(map #(.. data-2010 (attribute %) name) (range (.numAttributes data-2010)))




;;;;;;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;;

(nth [1 2 3 4] 2)

(name :key)


;..

(.. r/data (attribute 3) name)

;is a short form of

(. (. r/data (attribute 3)) name)


(. r/data (attribute 3))

;is the expanded form of macro

(.attribute r/data 3)

;(join separator coll)

(s/join "," [1 2 3 4 5])
(s/join \, [1 2 3 4 5])
