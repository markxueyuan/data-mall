(ns data-mall.sampling)

(defn sample-percent
  [percent coll]
  (filter (fn [_] (<= (rand) percent)) coll))

