(ns weka.k-means
  (:import [weka.core EuclideanDistance]
           [weka.clusterers SimpleKMeans])
  (:require [weka.readCSV :as r]))

(r/load-arff "D:/data/iris.arff")

(defn random-seed
  [seed]
  (if (nil? seed)
    (.. (java.util.Date.) getTime intValue)
    ))

(random-seed nil)
