(ns weka.decision_tree
  (:import [weka.classifiers.trees J48])
  (:require [weka.readCSV :as r]
            [clojure.java.io :as io]))

(defn random-seed
  [seed]
  (if (nil? seed)
    (.. (java.util.Date.) getTime intValue)
    seed
    ))

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


(defanalysis j48
  J48 buildClassifier
  [["-U" pruned true :flag-flase]
   ;["-C" confidence 0.25]
   ["-M" min-instances 2]
   ["-R" reduced-error false :flag-true]
   ["-N" folds 3 :predicate reduced-error]
   ["-B" binary-only false :flag-true]
   ["-S" subtree-raising true :flag-false]
   ["-L" clean true :flag-false]
   ["-A" smoothing true :flag-true]
   ["-J" mdl-correction true :flag-false]
   ["-Q" seed 1 random-seed]])

(def shrooms (doto (r/load-arff "D:/data/mushroom.arff")
               (.setClassIndex 22)))

(def d-tree (j48 shrooms :pruned true))

(with-open [w (io/writer "D:/data/decision-tree.gv")]
  (.write w (.graph d-tree)))

(r/load-arff "D:/data/mushroom.arff")
