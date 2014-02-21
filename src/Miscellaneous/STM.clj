(ns Miscellaneous.STM)

(require '[clojure.java.io :as javaio]
         '[clojure.data.csv :as csv]
         '[incanter.io :as io]
         '[incanter.core :as incanter])

(def data-file "D:/data/all_160_in_51.P35.csv")


;;;;;;;;;;;;;;;fn;;;;;;;;;;;;;;;;;;;;;;;;

(declare total-hu total-fams)

(defn lazy-read-csv
  [csv-file]
  (let [in-file (javaio/reader csv-file)
        csv-seq (csv/read-csv in-file)
        lazy (fn lazy [wrapped]
               (lazy-seq
                (if-let [s (seq wrapped)]
                  (cons (first s) (lazy (rest s)))
                  (.close in-file))))]
    (lazy csv-seq)))

(defn with-header [coll]
  (let [headers (map keyword (first coll))]
    (map (partial zipmap headers) (next coll))))

(defn ->int ([i] (Integer. i)))

(defn sum-item
  ([fields] (partial sum-item fields))
  ([fields accum item]
   (mapv + accum (map ->int (map item fields)))))

(defn sum-items
  [accum fields coll]
  (reduce (sum-item fields) accum coll))

(defn update-totals [fields items]
  (let [mzero (map (constantly 0) fields)
        [sum-hu sum-fams] (sum-items mzero fields items)]
    (dosync (alter total-hu #(+ sum-hu %))
            (alter total-fams #(+ sum-fams %)))))

(defn thunk-update-totals-for
  [fields data-chunk]
  (fn [] (update-totals fields data-chunk)))

(defn main
  ([data-file] (main data-file [:HU100 :P035001] 5))
  ([data-file fields chunk-count]
   (doall
    (->>
     (lazy-read-csv data-file)
     with-header
     (partition-all chunk-count)
     (map (partial thunk-update-totals-for fields))
     (map future-call)
     (map deref)
     ))
   (float (/ @total-fams @total-hu))))

(defn anatomy
  ([data-file] (anatomy data-file [:HU100 :P035001] 5))
  ([data-file fields chunk-count]
   (doall
    (->>
     (lazy-read-csv data-file)
     with-header
     (partition-all chunk-count)
     (map (partial thunk-update-totals-for fields))
     (map future-call)
     (map deref)))))
;;;;;;;;;;;;;;;working zone;;;;;;;;;;;;;;;;;

#_(map #(zipmap (map keyword (first (lazy-read-csv data-file))) %)
     (rest (lazy-read-csv data-file)))

#_(map keyword (first (lazy-read-csv data-file)))

(def total-hu (ref 0))

(def total-fams (ref 0))

(main data-file)

(anatomy data-file[:HU100 :P035001] 100)
;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;

;turn string to integer

(Integer. "8")

;how to make a constant function?

(constantly 0)

;don't forget that maps are also functions

(map {:a 2 :b 3 :c 4} [:a :c])


;difference between partition-all and partition

(partition-all 3 [1 2 3 4 5 6 7 8])
(partition 3 [1 2 3 4 5 6 7 8])

;;;;what does  fn future-call exactly mean?


;Takes a function of no args and yields a future object that will
;invoke the function in another thread, and will cache the result and
;return it on all subsequent calls to deref/@. If the computation has
;not yet finished, calls to deref/@ will block, unless the variant
;of deref with timeout is used. See also - realized?.

;if you understand this, then you will figure out the function of (map deref) in the above fn main.

;;;;;;;;;;;;;not that smart;;;;;;;;;;;;;;;















