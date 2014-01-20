(ns data-mall.zipper)

(require '[clojure.zip :as zip])

(def v [[1 2 [3 4]] [5 6]])

(-> v
    zip/vector-zip
    zip/down
    zip/right
    (zip/replace [77 88])
    zip/node)

(-> v
    zip/vector-zip
    zip/down
    zip/right
    (zip/replace [77 88])
    zip/root)

(-> v
    zip/vector-zip
    zip/down
    zip/down
    zip/right
    zip/remove
    zip/root)

(-> v
    zip/vector-zip
    zip/down
    zip/down
    zip/right
    (zip/edit * 55)
    zip/root)

(->> v
    zip/vector-zip
    zip/children
    (iterate next)
     (take-while #(not (nil? %)))
     )

(->> v
     zip/vector-zip
     zip/children
     )






