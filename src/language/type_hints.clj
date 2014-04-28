(ns language.type-hints
  (:require [criterium.core :as c]))

(defn rand-point [] [(rand) (rand)])


(defn center-distance
  [[x y]]
  (Math/sqrt (+ (* x x) (* y y))))
