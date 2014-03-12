(ns language.protocol)


(defprotocol Matrix
  "Protocol for working with 2d data structures"
  (lookup [matrix i j])
  (update [matrix i j val])
  (rows [matrix])
  (cols [matrix])
  (dims [matrix]))

(extend-protocol Matrix

  clojure.lang.IPersistentVector
  (lookup [vov x y]
          (get-in vov [x y]))
  (update [vov x y val]
          (assoc-in vov [x y] val))
  (rows [vov]
        (seq vov))
  (cols [vov]
        (apply map vector vov))
  (dims [vov]
        [(count vov) (count (first vov))])

  nil
  (lookup [x i j])
  (update [x i j val])
  (rows [x] [])
  (cols [x] [])
  (dims [x] [0 0])

  (Class/forName "[[D")
  (lookup [arr i j]
          (aget arr i j))
  (update [arr i j val]
          (let [clone (aclone arr)]
            (aset clone i
                  (doto (aclone (aget clone i))
                    (aset j val)))
            clone))
  (rows [arr]
        (map vec arr))
  (cols [arr]
        (apply map vector arr))
  (dims [arr]
        (let [rs (count arr)]
          (if (zero? rs)
            [0 0]
            [rs (count (aget arr 0))]))))


(defn vov [h v]
  "create a vector of h v-item vectors"
  (vec (repeat h (vec (repeat v nil)))))

;(update [[1 2 3] [4 5 9] [6 7 8]] 2 1 100)
;(dims [[1 2 3] [4 5 9] [6 7 8]])
;(cols nil)
;(dims nil)
;(def vov-a (vov 3 5))
;(update vov-a 2 4 3)
;(cols (update vov-a 2 4 3))






;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;

(vector 1 2 3)
(vec "123")
(apply map vector [[1 2 3] [4 5 6] [7 8]])



