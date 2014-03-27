(ns Miscellaneous.tryOO)

(defprotocol Inc
  (increase [this])
  (read-it [this]))


(deftype Counter [num])

(extend-type Counter
  Inc
  (increase [this] (Counter. (inc (.num this))))
  (read-it [this] (.num this)))

(def a (Counter. 3))

(read-it (increase a))

(def x (ref 0))
