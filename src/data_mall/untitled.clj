(require '[clojure.java.io :as javaio]
         '[clojure.data.csv :as csv]
         '[incanter.io :as io]
         '[incanter.core :as incanter])

(def data-file "D:/data/all_160_in_51.P35.csv")

(-> data-file
    (io/read-dataset :header true)
    (incanter/view))

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
   (doall)))






;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;

;turn string to integer

(Integer. "8")

;how to make a constant function?

(constantly 0)






