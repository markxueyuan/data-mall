(ns language.array)

;make a random histogram without using array

(defn vector-histogram
  [data]
  (reduce (fn [hist val]
            (update-in hist [val] inc))
          (vec (repeat 10 0))
          data))

;(def data (doall (repeatedly 1e6 #(rand-int 10))))

;(time (vector-histogram data))

;uses an array

(defn array-histogram
  [data]
  (vec
   (reduce (fn [^longs hist val]
             (aset hist val (inc (aget hist val)))
             hist)
           (long-array 10)
           data
           )))

;(time (array-histogram data))

;return an array of objects

;(to-array [:a :b :c])

;return an array of specified type
#_(into-array CharSequence ["a" "b" "c"])

;produce array of primitive type using boxed type
#_(into-array Long/TYPE (range 5))

#_(seq (long-array 20 (range 10)))

#_(def arr (make-array String 5 5))
#_(aget arr 0 0)

#_(def arr (make-array Boolean/TYPE 10))

#_(aget arr 9)

;amap and areduce

#_(seq (let [a (int-array (range 10))]
  (amap a i res
        (inc (aget a i)))))


#_(let [a (int-array (range 10))]
  (areduce a i sum 0
          (+ sum (aget a i))))

#_(def arr (make-array Double/TYPE 1000 1000))

;you have to do more to deal with the reflections in multidimentional arrays

#_(time (dotimes [i 1000]
        (dotimes [j 1000]
          (aset arr i j 0.1)
          (aget arr i j))))

#_(time (dotimes [i 1000]
        (dotimes [j 1000]
          (let [^doubles darr (aget ^objects arr i)]
             (aset darr j 1.0)
             (aget darr j)))))

;two important macros to set and get multi-dims array.

(defmacro deep-aget
  "Gets a value from a multidimensional array as if via `aget`,
  but with automatic application of appropriate type hints to
  each step in the array traversal as guided by the hint added
  to the source array.
  e.g. (deep-aget ^doubles arr i j)"
  ([array idx]
   `(aget ~array ~idx))
  ([array idx & idxs]
   (let [a-sym (gensym "a")]
     `(let [~a-sym (aget ~(vary-meta array assoc :tag 'objects) ~idx)]
        (deep-aget ~(with-meta a-sym {:tag (-> array meta :tag)}) ~@idxs)))))

(defmacro deep-aset
  "Sets a value in a multidimensional array as if via `aset`,
  but with automatic application of appropriate type hints to
  each step in the array traversal as guided by the hint added
  to the target array.
  e.g. (deep-aset ^doubles arr i j 1.0)"
  [array & idxsv]
  (let [hints '{booleans boolean, bytes byte
                chars char, longs long
                ints int, shorts short
                doubles double, floats float}
        hint (-> array meta :tag)
        [v idx & sxdi] (reverse idxsv)
        idxs (reverse sxdi)
        v (if-let [h (hints hint)] (list h v) v)
        nested-array (if (seq idxs)
                       `(deep-aget ~(vary-meta array assoc :tag 'objects) ~@idxs)
                       array)
        a-sym (gensym "a")]
    `(let [~a-sym ~nested-array]
       (aset ~(with-meta a-sym {:tag hint}) ~idx ~v))))










