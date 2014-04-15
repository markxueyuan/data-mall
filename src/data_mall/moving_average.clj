(ns data-mall.moving-average)

(defn average
  [coll]
  (/ (reduce + coll) (count coll)))

