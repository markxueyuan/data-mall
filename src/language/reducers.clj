(ns language.reducers
  (:require [clojure.core.reducers :as r]
            [criterium.core :as c]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;the elements of reducers;;;;;;;;;;;;;;;;;;;;;;

;reducer is high-order function

;step 1

(defn mapping [f]
  (fn [f1];(f1 result input) -> new-result
    (fn [result input]
      (f1 result (f input)))))

(defn filtering [pred]
  (fn [f1]
    (fn [result input]
      (if (pred input)
        (f1 result input)
        result))))

(defn mapcatting [f]
  (fn [f1]
    (fn [result input]
      (reduce f1 result (f input)))))

;mapcatting and kin can produce more than one result per input by simply operating on result more than once

(reduce + 0 (map inc [1 2 3 4]))
;equals
(reduce ((mapping inc) +) 0 [1 2 3 4])

;step 2

(defn reducer
  [coll xf]
  (reify
    clojure.core.protocols/CollReduce
    (coll-reduce
     [_ f1 init]
     (clojure.core.protocols/coll-reduce coll (xf f1) init))))

(reduce + 0 (map inc [1 2 3 4]))
;equals now
(reduce + 0 (reducer [1 2 3 4] (mapping inc)))


;It feels as if we have transformed the collection itself

;step 3

(defn rmap [f coll]
  (reducer coll (mapping f)))

(defn rfilter [pred coll]
  (reducer coll (filtering pred)))

(defn rmapcat [f coll]
  (reducer coll (mapcatting f)))


;try it!
(reduce + 0 (rmap inc [1 2 3 4]))

(reduce + 0 (rfilter even? [1 2 3 4]))

(reduce + 0 (rmapcat range [1 2 3 4 5]))

;foldable

;As long as the transformation itself doesn't care about order (e.g. as take does), then a reducer is as foldable as its source

(defn folder
  ([coll xf]
     (reify
       clojure.core.protocols/CollReduce
       (coll-reduce
        [_ f1 init]
        (clojure.core.protocols/coll-reduce coll (xf f1) init))
       r/CollFold
       (r/coll-fold
        [_ n combinef reducef]
        (r/coll-fold coll n combinef (xf reducef))))))

(defn rmap [f coll]
  (folder coll (mapping f)))

(defn rfilter [pred coll]
  (folder coll (filtering pred)))

(defn rmapcat [f coll]
  (folder coll (mapcatting f)))



(defn rand-point [] [(rand) (rand)])


(defn center-distance
  [[x y]]
  (Math/sqrt (+ (* x x) (* y y))))

;sequencing way

(defn count-in-circle
  [n]
  (->> (repeatedly n rand-point)
       (map center-distance)
       (filter #(<= % 1.0))
       count))

(defn mc-pi
  [n]
  (* 4.0 (/ (count-in-circle n) n)))

;reducing way

(defn count-items [c _] (inc c))

(defn count-in-circle-r
  [n]
  (->> (repeatedly n rand-point)
       vec;reducer works in tree-style data structure
       (r/map center-distance)
       (r/filter #(<= % 1.0));reducers are curried
       (r/fold + count-items)));count-items is the combine fn.

(defn mc-pi-r
  [n]
  (* 4.0 (/ (count-in-circle-r n) n)))

;comparison

#_(c/quick-bench (mc-pi 3000000))

#_(c/quick-bench (mc-pi-r 3000000))

;可是搞了半天比原来还慢

;;;;;;;;;;tips;;;;;;;;;;;;;
;identity
(+)
(*)
