(ns language.oneline-summary
  (:require [clojure.core.reducers :as r]))

(def zero-counts
  {:n 0 :s 0.0 :mean 0.0 :m2 0.0})

(defn accum-counts
  ([] zero-counts)
  ([{:keys [n mean m2 s] :as accum} x]
   (let [new-n (inc n)
         delta (- x mean)
         delta-n (/ delta new-n)
         ter])))



