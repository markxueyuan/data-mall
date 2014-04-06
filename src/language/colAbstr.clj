(ns language.colAbstr)

(defn scaffold
  [interface]
  (doseq [[iface methods] (->> interface
                               .getMethods
                               (map #(vector (.getName (.getDeclaringClass %))
                                             (symbol (.getName %))
                                             (count (.getParameterTypes %))))
                               (group-by first))]
    (println (str " " iface))
    (doseq [[_ name argcount] methods]
      (println (str "     "
                    (list name (into '[this] (take argcount (repeatedly gensym)))))))))

(ancestors (class #{}))

(scaffold clojure.lang.IPersistentSet)
;An array backed set implementation using deftype

(declare empty-array-set)

(def ^:private ^:constant max-size 4)

(deftype ArraySet
  [^objects items ^int size ^:unsynchronized-mutable ^int hashcode]
  clojure.lang.IPersistentSet
  (get
   [this x]
   (loop [i 0]
     (when (< i size)
       (if (= x (aget items i))
         (aget items i)
         (recur (inc i))))))
  (contains
   [this x]
   (boolean (loop [i 0]
              (when (< i size)
                (or (= x (aget items i)) (recur (inc i)))))))
  (disjoin
   [this x]
   (loop [i 0]
     (if (== i size)
       this
       (if (not= x (aget items i))
         (recur (inc i))
         (ArraySet. (doto (aclone items)
                      (aset i (aget items (dec size)))
                      (aset (dec size) nil))
                    (dec size)
                    -1)))))
  clojure.lang.IPersistentCollection
  (count
   [this] size)
  (cons
   [this x]
   (cond
    (.contains this x) this
    (== size max-size) (into #{x} this)
    :else (ArraySet. (doto (aclone items)
                       (aset size x))
                     (inc size)
                     -1)))
  (empty [this] empty-array-set)
  (equiv [this that] (.equals this that))
  clojure.lang.Seqable
  (seq [this] (take size items))
  Object
  (hashCode
   [this]
   (when (== -1 hashcode)
     (set! hashcode (int (areduce items idx ret 0
                                  (unchecked-add-int ret (hash (aget items idx)))))))
   hashcode)
  (equals
   [this that]
   (or (identical? this that)
       (and (or (instance? java.util.Set)
                (instance? clojure.lang.IPersistentSet that));this can be cancelled if ArraySet implements java.util.Set
            (= (count this) (count that))
            (every? #(contains? this %) that))))
  clojure.lang.IFn
  (invoke
   [this key]
   (.get this key))
  (applyTo
   [this args]
   (when (not= 1 (count args))
     (throw (clojure.lang.ArityException. (count args) "ArraySet")))
   (this (first args)))
  java.util.Set
  (isEmpty
   [this]
   (zero? size))
  (size
   [this]
   size)
  (toArray
   [this array]
   (.toArray ^java.util.Collection (sequence items) array))
  (toArray
   [this]
   (into-array (seq this)))
  (iterator
   [this]
   (.iterator ^java.util.Collection (sequence this)))
  (containsAll
   [this coll]
   (every? #(contains? this %) coll)))

(def ^:private empty-array-set (ArraySet. (object-array max-size) 0 -1))

(defn array-set
  "Creats an array backed set containing the given values."
  [& vals]
  (into empty-array-set vals))

(array-set)

(conj (array-set) 1)

(apply array-set "hello")


(apply array-set "helloha")

(into #{1 2 3 4} "seq")

(.cons (.cons (apply array-set "hell") "os") "habi")

(.equals (array-set) #{})

(.equals #{} (array-set))

(scaffold java.util.Set)

(= #{3 1 2 0} (array-set 3 1 0 2))

((apply array-set "hello") \h)

(defn microbenchmark
  [f & {:keys [size trials] :or {size 4 trials 1e6}}]
  (let [items (repeatedly size gensym)]
    (time (loop [s (apply f items) n trials]
            (when (pos? n)
              (doseq [x items]
                (contains? s x))
              (let [x (rand-nth items)]
                (recur (-> s (disj x) (conj x)) (dec n))))))))

(doseq [n (range 1 5)
        f [#'array-set #'hash-set]]
  (print n (-> f meta :name) ": ")
  (microbenchmark @f :size n))




;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;;

;fn boolean coerce to boolean

(boolean "")
(boolean nil)
(boolean false)
(boolean true)
(boolean {})

;into-array

;Returns an array with components set to the values in aseq. The array's
;component type is type if provided, or the type of the first value in
;aseq if present, or Object. All values in aseq must be compatible with
;the component type. Class objects for the primitive types can be obtained
;using, e.g., Integer/TYPE.

;The following cause an exception

#_(into-array [2 "4" "8" 5])

;while this is correct:

(into-array Object [2 "4" "8" 5])

;it is said that the type should be what the element is,so:

(into-array (range 4));is correct,while
(into-array Byte/TYPE (range 4));causes exception
;however, both works, so you don't bother to coerce the element to the main type:
(into-array Byte/TYPE (map byte (range 4)))

;difference between sequence and seq is little
(sequence nil)
(seq nil);which may cause a null pointer exception






