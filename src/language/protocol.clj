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

;inline implementation

(defprotocol ClashWhenInlined
  (size [x]))

#_(defrecord R []
  ClashWhenInlined
  (size [x]))


(deftype R []
  ClashWhenInlined
  (size [x]))

(defrecord R [])

(extend-type R
  ClashWhenInlined
  (size [x]))

(.size (R.));this result is exceptional!

;inline implementation is not encouraged! But it is the only way to implement java interfaces!


;extend Object by inline implementation(this is a special case, inline implementation does not support other classes)

(deftype Point [x y]
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
  (dims [pt] [2 1])
  Object
  (equals [this other]
          (and (instance? (class this) other)
               (= x (.x other))
               (= y (.y other))))
  (hashCode [this]
            (-> x
                hash
                (hash-combine y))))

(Point. 3 4)

;(.hashCode (Point. 3 4))
;(.hashCode (Point. 4 3))

;(.equals (Point. 3 4) (Point. 3 4))


;reify
(count (.listFiles (java.io.File. ".")
            (reify
              java.io.FileFilter
              (accept [this f]
                      (.isDirectory f)))))

(aget (.listFiles (java.io.File. ".")) 0)

(count (.listFiles (java.io.File. ".")))



(defrecord Banana [qty])
(defrecord Grape  [qty])
(defrecord Orange [qty])

;;; 'subtotal' differs from each fruit.

(defprotocol Fruit
  (subtotal [item]))

(extend-type Banana
  Fruit
  (subtotal [item]
            (* 158 (:qty item))))

(extend-type Grape
  Fruit
  (subtotal [item]
            (* 178 (:qty item))))

(extend-type Orange
  Fruit
  (subtotal [item]
            (* 98 (:qty item))))

;;; 'coupon' is the function returing a 'reify' of subtotal. This is
;;; when someone uses a coupon ticket, the price of some fruits is
;;; taken off 25%.

(defn coupon [item]
  (reify Fruit
    (subtotal [_]
              (int (* 0.75 (subtotal item))))))

;;; Example: To compute the total when someone bought 10 oranges,
;;;  15 bananas and 10 grapes using a coupon.
(apply +  (map subtotal [(Orange. 10) (Banana. 15) (coupon (Grape. 10))]))


;reusing implementations

(defrecord Point [x y])

(extend Point
  Matrix
  {:lookup (fn [pt i j]
             (when (zero? j)
               (case i
                 0 (:x pt)
                 1 (:y pt))))
   :update (fn [pt i j val]
             (if (zero? j)
               (case i
                 0 (Point. val (:y pt))
                 1 (Point. (:x pt) val))
               pt))
   :rows (fn [pt]
           [[:x pt] [:y pt]])
   :cols (fn [pt]
           [(:x pt) (:y pt)])
   :dims (fn [pt]
           [2 1])})

;(lookup (Point. 3 4) 1 0)

;like heritage from discrete class implementation

(def abstract-matrix-impl
  "happily fixed the problems in book clojure programming"
  {:rows (fn [matrix]
           (let [[h v] (dims matrix)]
             (map (fn [x] (map #(lookup matrix x %) (range 0 v))) (range 0 h))))
   :cols (fn [matrix]
           (apply map vector (rows matrix)))})

(extend Point
  Matrix
  (assoc abstract-matrix-impl
    :lookup (fn [pt i j]
             (when (zero? j)
               (case i
                 0 (:x pt)
                 1 (:y pt))))
    :update (fn [pt i j val]
             (if (zero? j)
               (case i
                 0 (Point. val (:y pt))
                 1 (Point. (:x pt) val))
               pt))
    :dims (fn [pt]
           [2 1])))

(rows (Point. 3 4))

(cols (Point. 3 4))

;mixins

(defprotocol Measurable
  "A protocol for retrieving the dimensions of widget"
  (width [measurable] "Returns the width in px")
  (height [measurable] "Returns the height in px"))

(defrecord Button [text])

(:text (Button. "hahaha"))

(extend-type Button
  Measurable
  (width [button]
         (* 8 (count (:text button))))
  (height [button]
          8))

(width (Button. "This is not kidding!"))
(height (Button. "This is not kidding!"))

(def bordered
  {:width #(* 2 (:border-width %))
   :height #(* 2 (:border-height %))})

Measurable
(get-in Measurable [:impls Button])

(defn combine
  [op f g]
  (fn [& args]
    (op (apply f args) (apply g args))))

(defrecord BorderedButton [text border-width border-height])

(extend BorderedButton
  Measurable
  (merge-with (partial combine +)
              (get-in Measurable [:impls Button])
              bordered))

(width (Button. "What a mother fucker you are!"))
(height (Button. "What a mother fucker you are!"))

(width (BorderedButton. "What a mother fucker you are!" 9 1))
(height (BorderedButton. "What a mother fucker you are!" 9 1))








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

;size is defined as a method for maps

(.size {:a 2 :b 3 :c 4 :d 4})

;instance?
(instance? (class 3) 2)

(class 3)

(instance? (class (Integer. 3)) 3)

(hash 'x)
(hash-combine (hash 'x) 'y)

;range forever returns a seq
(range 0 1)
