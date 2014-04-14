(ns weka.column
  (:import [weka.filters Filter]
           [weka.filters.unsupervised.attribute Remove])
  (:require [clojure.string :as s]
            [weka.readCSV :as r]))

(defn set-fields
  ([instances field-seq]
   (doseq [n (range (.numAttributes instances))]
     (.renameAttribute instances
                       (.attribute instances n)
                       (name (nth field-seq n))))))

(map #(.. r/data (attribute %) name)
     (range (.numAttributes r/data)))




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
