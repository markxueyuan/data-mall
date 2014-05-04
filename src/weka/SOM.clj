(ns weka.SOM
  (:require [incanter.core :as core]
            [incanter.som :as som]
            [incanter.datasets :as ds]))

(def iris (ds/get-dataset :iris))

(core/view iris)

(def iris-clusters
  (som/som-batch-train
   (core/to-matrix
    (core/sel iris :cols [:Sepal.Length :Sepal.Width
                          :Petal.Length :Petal.Width]))))

iris-clusters


(for [[pos rws] (:sets iris-clusters)]
  (str pos \: (frequencies (core/sel iris :cols :Species :rows rws))))

(:dims iris-clusters)
