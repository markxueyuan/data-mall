(ns weka.hierarchical
  (:require [weka.k-means :as macro]
            [weka.readCSV :as r]
            [weka.column :as c]
            [clojure.string :as s])
  (:import [weka.core EuclideanDistance]
           [weka.clusterers HierarchicalClusterer]))

(def iris (r/load-arff "D:/data/iris.arff"))



(macro/defanalysis hierarchical HierarchicalClusterer buildClusterer
                   [["-A" distance EuclideanDistance .getName]
                    ["-L" link-type :centroid #(s/upper-case (s/replace (name %) \- \_))]
                    ["-N" k nil :not-nil]
                    ["-D" verbose false :flag-true]
                    ["-B" distance-of :node-length :flag-equal :branch-length]
                    ["-P" print-newick false :flag-true]])

(def iris-petal (c/filter-attributes iris [:sepallength :sepalwidth :class]))

(def hc (hierarchical iris-petal :k 3 :print-newick true))

(.clusterInstance hc (.get iris-petal 149))
