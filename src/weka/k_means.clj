(ns weka.k-means
  (:import [weka.core EuclideanDistance]
           [weka.clusterers SimpleKMeans])
  (:require [weka.readCSV :as r]
            [weka.column :as c]))

(def iris (r/load-arff "D:/data/iris.arff"))

(def iris-petal (c/filter-attributes iris [:sepallength :sepalwidth :class]))

(map #(.. iris-petal (attribute %) name)
     (range (.numAttributes iris-petal)))

(defn random-seed
  [seed]
  (if (nil? seed)
    (.. (java.util.Date.) getTime intValue)
    seed
    ))



;(random-seed nil)

(defn analysis-parameter
  [parameter]
  (condp = (count parameter)
    3 `[~(first parameter) ~(second parameter)]
    4 (condp = (last parameter)
        :flag-true `[(when ~(second parameter)
                       ~(first parameter))]
        :flag-false `[(when-not ~(second parameter)
                        ~(first parameter))]
        :not-nil `[(when-not (nil? ~(second parameter))
                     [~(first parameter) ~(second parameter)])]
        :seq (let [name (second parameter)]
               (apply concat
                      (map-indexed (fn [i flag] `[~flag (nth ~name ~i)]) (first parameter))))
        `[~(first parameter) (~(last parameter) ~(second parameter))])
    5 (condp = (nth parameter 3)
        :flag-equal `[(when (= ~(second parameter) ~(last parameter))
                        ~(first parameter))]
        :predicate `[(when ~(last parameter)
                       [~(first parameter) ~(second parameter)])])))

(defmacro defanalysis
  ([a-name a-class a-method parameters]
   `(defn ~a-name
      [dataset# & {:keys ~(mapv second parameters)
                   :or ~(into {} (map #(vector (second %) (nth % 2)) parameters))}]
      (let [options# (r/->options ~@(mapcat analysis-parameter parameters))]
        (doto (new ~a-class)
          (.setOptions options#)
          (. ~a-method dataset#))))))

(defanalysis k-means SimpleKMeans buildClusterer [["-N" k 2]
                                                  ["-I" max-iterations 100]
                                                  ["-V" verbose false :flag-true]
                                                  ["-S" seed 1 random-seed]
                                                  ["-A" distance EuclideanDistance .getName]])

(macroexpand-1 (defanalysis k-means SimpleKMeans buildClusterer [["-N" k 2]
                                                  ["-I" max-iterations 100]
                                                  ["-V" verbose false :flag-true]
                                                  ["-S" seed 1 random-seed]
                                                  ["-A" distance EuclideanDistance .getName]]))

;(def km (k-means iris-petal :k 3))

;km

;(def testcsv (r/load-csv "D:/data/testkmeans.csv"))

;(k-means testcsv)

;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;

;the whole shit is just this:

#_(def a (->options "-N" 3 "-I" 100 "-S" 1 "-A" (.getName EuclideanDistance)))

#_(doto (SimpleKMeans.)
  (.setOptions a)
  (.buildClusterer iris-petal))

#_(def b (SimpleKMeans.))

#_(doto b
  (.setOptions a)
  (.buildClusterer iris-petal)
  (.setPreserveInstancesOrder true))

#_(.getCapabilities b)

;condp
(condp some [1 2 3 4]
  #{0 6 7} :>> inc
  #{5 9} :>> dec
  #{8 10} :>> #(+ % 3)
  "helloha"
  )

;map-indexed
(map-indexed (fn [index item] [index item]) "hello")
(map-indexed vector "hello")


