(ns weka.hierarchical
  (:require [weka.k-means :as macro]
            [weka.readCSV :as r]
            [clojure.string :as s])
  (:import [weka.core EuclideanDistance]
           [weka.clusterers HierarchicalClusterer]))

(def iris (r/load-arff "D:/data/iris.arff"))

(macro/defanalysis hierarchical HierarchicalClusterer buildClusterer
                   [["-A" distance EuclideanDistance .getName]])
