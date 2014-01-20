(ns Miscellaneous.STM)

(require '[clojure.java.io :as javaio]
         '[clojure.data.csv :as csv]
         '[incanter.io :as io]
         '[incanter.core :as incanter])

(def data-file "D:/data/all_160_in_51.P35.csv")

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

(def total-hu (ref 0))

(def total-fams (ref 0))

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
     (map deref)))
   (float (/ @total-fams @total-hu))))


;(main data-file)


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

;;;;;;;;;;;;;not that smart;;;;;;;;;;;;;;;


while











