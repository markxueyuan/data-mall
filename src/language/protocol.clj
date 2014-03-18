(ns language.protocol)

;extend protocols to existing types
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

  #_((Class/forName "[[D")
  (lookup [arr i j]
          (aget arr i j))
  (update [arr i j val]
          (aset arr i j val))
  (rows [arr]
        (map vec arr))
  (cols [arr]
        (apply map vector arr))
  (dims [arr]
        (let [rs (count arr)]
          (if (zero? rs)
            [0 0]
            [rs (count (aget arr 0))]))))

)


(defn vov [h v]
  "create a vector of h v-item vectors";factory function
  (vec (repeat h (vec (repeat v nil)))))

;(update [[1 2 3] [4 5 9] [6 7 8]] 2 1 100)
;(dims [[1 2 3] [4 5 9] [6 7 8]])
;(cols nil)
;(dims nil)
;(def vov-a (vov 3 5))
;(update vov-a 2 4 3)
;(cols (update vov-a 2 4 3))


;define records

(defrecord NamedPoint [^String name ^long x ^long y])

(NamedPoint/getBasis)

(map meta (NamedPoint/getBasis))

(->NamedPoint "haha" 3 4)

(map->NamedPoint {:name "haha" :x 2 :y 4})

;define types

(deftype Point [x y])

(def bb (Point. 3 4))

(.x bb)

(.y bb)

;

(deftype MyType [^:volatile-mutable fld])

;schrödinger's cat

(deftype SchrödingerCat [^:unsynchronized-mutable state]
  clojure.lang.IDeref
  (deref [sc]
         (locking sc
           (or state
               (set! state (if (zero? (rand-int 2))
                             :dead
                             :alive))))))

(defn schrödinger-cat
  []
  (SchrödingerCat. nil))

(def daguguguji (schrödinger-cat))

@daguguguji


;implement protocols

(defrecord Point [x y]
  Matrix
  (lookup [pt i j]
          (when (zero? j)
            (case i
              0 x
              1 y)))
  (update [pt i j val]
          (if (zero? j)
            (condp = i
              0 (Point. val y)
              1 (Point. x val))
            pt))
  (rows [pt] [[x] [y]])
  (cols [pt] [x y])
  (dims [pt] [2 1]))

(lookup (Point. 3 4) 0 0)
(update (Point. 3 4) 1 0 7)

(defrecord Point [x y])

(extend-protocol Matrix
  Point
  (lookup [pt i j]
          (when (zero? j)
            (case i
              0 (.x pt)
              1 (.y pt))))
  (update [pt i j val]
          (if (zero? j)
            (condp i
              0 (Point. val (.y pt))
              1 (Point. (.x pt) val))
            pt))
  (rows [pt] [[(.x pt)] [(.y pt)]])
  (cols [pt] [(.x pt) (.y pt)])
  (dims [pt] [2 1]))

(lookup (Point. 3 4) 1 0)








;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;

(vector 1 2 3)
(vec "123")
(apply map vector [[1 2 3] [4 5 6] [7 8]])

;add a pre to your function

(defn try-pre
  [x]
  {:pre [(pos? x)]}
  (+ x 2))

#_(try-pre -2)
(try-pre 2)

;the following shows how a lock works

(def o (Object.))

(future (locking o
          (Thread/sleep 10000)
          (println "done!")))

(future (locking o
          (println "done!")))

;some

(some #{1 2 3} [3 4 5])

(some #{1 2 3} [4 5 6])

(some even? [4 5 6])

(some even? [5 7 9])

;condp

(condp some [2 3 4]
  ;#{0 1 2} "low!"
  ;#{2 3 4} :>> #(str "higher than " %)
  #{3 4 5} :>> #(str "find this -> " %)
  #{4 5 6} "high!")



