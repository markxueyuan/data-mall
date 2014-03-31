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


(scaffold clojure.lang.IPersistentSet)
;An array backed set implementation using deftype

(declare empty-erray-set)

(def ^:private ^:constant max-size 4)

(deftype ArraySet
  [^objects items ^int size ^unsynchronized-mutable ^int hashcode]
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
  )


;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;;

;fn boolean coerce to boolean

(boolean "")
(boolean nil)
(boolean false)
(boolean true)
(boolean {})
