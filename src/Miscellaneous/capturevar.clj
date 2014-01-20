(ns Miscellaneous.capturevar)

(defn a [b] (+ 5 b))

(def b (partial a 5))

(b)

(def c (partial #'a 5))

(c)


(defn a [b] (+ 10 b))

(b)

(c)
