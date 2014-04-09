(ns data-mall.bench
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


;;;;;;;;;;;;let's bench!;;;;;;;;;;;;;;;;;

(def chunk-size 4096)

(def input-size 1000000)

(c/quick-bench (mc-pi input-size))

(c/quick-bench (mc-pi-pmap input-size))

(c/quick-bench (mc-pi-part input-size))

(mc-pi input-size)

(c/bench (mc-pi input-size))























