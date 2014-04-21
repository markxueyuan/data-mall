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
         term-1 (* delta delta-n n)
         new-mean (+ mean delta-n)]
     {:n new-n
      :mean new-mean
      :s (+ s x)
      :m2 (+ m2 term-1)})))

(defn op-fields
  "A utility function that calls a function on the values of a field from two maps"
  [op field item1 item2]
  (op (field item1) (field item2)))

(defn combine-counts
  ([] zero-counts)
  ([xa xb]
   (let [n (long (op-fields + :n xa xb))
         delta (op-fields - :mean xb xa)
         nxa*xb (*' (:n xa) (:n xb))]
     {:n n
      :mean (+ (:mean xa) (* delta (/ (:n xb) n)))
      :s (op-fields + :s xa xb)
      :m2 (+ (:m2 xa) (:m2 xb)
             (* delta delta (/ nxa*xb n)))})))

(defn stats-from-sums
  [{:keys [n mean m2 s] :as sums}]
  {:mean (double (/ s n))
   :variance (/ m2 (dec n))})

(defn summary-statistics
  [coll]
  (stats-from-sums
   (r/fold combine-counts accum-counts coll)))

(summary-statistics (repeatedly 10000000 rand))



