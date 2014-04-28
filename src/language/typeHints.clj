(ns language.typeHints
  (:require [criterium.core :as c]))

(defn rand-point [] [(rand) (rand)])


(defn center-distance
  [[x y]]
  (Math/sqrt (+ (* x x) (* y y))))

(defn mc-pi
  [n]
  (let [in-circle (->> (repeatedly n rand-point)
                       (map center-distance)
                       (filter #(<= % 1.0))
                       count)]
    (* 4.0 (/ in-circle n))))

(c/quick-bench (mc-pi 100000))


(defn center-distance-hint
  (^double [[x y]]
  (Math/sqrt (+ (* x x) (* y y)))))

(defn mc-pi-hint
  (^double [n]
  (let [in-circle (->> (repeatedly n rand-point)
                       (map center-distance-hint)
                       (filter #(<= % 1.0))
                       count)]
    (* 4.0 (/ in-circle n)))))


(c/report-result (c/quick-benchmark (mc-pi-hint 100000) c/*default-quick-bench-opts*))



