(ns data-mall.anneal
  (:require [criterium.core :as c])
  (:import (java.lang Math)))

(defn rand-point [] [(rand) (rand)])


(defn center-distance
  [[x y]]
  (Math/sqrt (+ (* x x) (* y y))))

(defn count-in-circle
  [n]
  (->> (repeatedly n rand-point)
       (map center-distance)
       (filter #(<= % 1.0))
       count))

;1

(defn mc-pi
  [n]
  (* 4.0 (/ (count-in-circle n) n)))

;2

(defn in-circle-flag
  [p]
  (if (<= (center-distance p) 1.0) 1 0))


(defn mc-pi-pmap
  [n]
  (let [in-circle (->> (repeatedly n rand-point)
                       (pmap in-circle-flag)
                       (reduce + 0))]
    (* 4 (/ in-circle n))))

;3

(defn mc-pi-part
  ([n] (mc-pi-part 512 n))
  ([chunk-size n]
   (let [step (int (Math/floor (float (/ n chunk-size))))
         remainder (mod n chunk-size)
         parts (lazy-seq (cons remainder (repeat step chunk-size)))
         in-circle (reduce + 0 (pmap count-in-circle parts))]
     (* 4.0 (/ in-circle n)))))

;;;;;;;;;;;;;;;;;;;;;;;;let's anneal!;;;;;;;;;;;;;;;;;;;;;;

(defn annealing
  [initial max-iter max-cost neighbor-fn cost-fn p-fn temp-fn]
  (let [get-cost (memoize cost-fn)
        cost (get-cost initial)]
    (loop [state initial cost cost k 1 best-seq [{:state state :cost cost}]]
      (println '>>> 'sa k \. state \$ cost)
      (if (and (< k max-iter)
               (or (nil? max-cost)
                   (> cost max-cost)))
        (let [t (temp-fn (/ k max-iter))
              next-state (neighbor-fn state)
              next-cost (get-cost next-state)
              next-place {:state next-state :cost next-cost}]
          (if (> (p-fn cost next-cost t) (rand))
            (recur next-state next-cost (inc k) (conj best-seq next-place))
            (recur state cost (inc k) best-seq)))
        best-seq))))

(defn get-neighbor
  [state]
  (max 0 (min 20 (+ state (- (rand-int 11) 5)))))

(defn get-pi-cost
  [n state]
  (let [chunk-size (long (Math/pow 2 state))]
    (first (:mean (c/quick-benchmark (mc-pi-part chunk-size n) c/*default-quick-bench-opts*)))))

(defn should-move
  [c0 c1 t]
  (* t (if (< c0 c1) 0.25 1)))

(defn get-temp [r] (- 1.0 (float r)))


(annealing 18 20 nil get-neighbor (partial get-pi-cost 1000000) should-move get-temp)


(get-pi-cost 1000000 9)





;;;;;;;;;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;;;;;

;the good use of memoize

(defn add-huge [] (reduce + 0 (range 100000000)))

(def memo-huge (memoize add-huge))

#_(memo-huge)

#_(add-huge)

























