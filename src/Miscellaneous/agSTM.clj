(ns Miscellaneous.agSTM)

(require '[clojure.java.io :as javaio]
         '[clojure.data.csv :as csv]
         '[incanter.io :as io]
         '[incanter.core :as incanter])

(import '[java.lang Thread])

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

(defn accum-sums [a b] (mapv + a b))

(defn div-vec [[a b]] (float (/ a b)))

(defn force-value [a]
  (await a)
  @a)

(defn get-chunk
  [data-ref]
  (dosync
   (when-let
     [[s & ss] (seq @data-ref)]
     (ref-set data-ref ss)
     s)))

(defn update-totals
  [totals fields coll-ref counter-ref]
  (if-let [items (get-chunk coll-ref)]
    (do
      (send *agent* update-totals fields coll-ref counter-ref)
      (sum-items totals fields items))
    (do
      (dosync (commute counter-ref inc))
      totals)))

(defn block-to-done
  [counter agent-count]
  (loop []
    (when-not (= agent-count @counter)
      (Thread/sleep 500)
      (recur))))

(defn get-results
  [agents fields]
  (->> agents
  (map force-value)
  (reduce accum-sums (mapv (constantly 0) fields))
  (div-vec)))

(defn main
  ([data-file] (main data-file [:P035001 :HU100] 5 4))
  ([data-file fields agent-count chunk-count]
   (let [mzero (mapv (constantly 0) fields)
         agents (map agent
                     (take agent-count (repeat mzero)))
         data (with-header (lazy-read-csv data-file))
         data-ref (ref
                   (doall
                    (partition-all chunk-count data)))
         finished (ref 0)]
     (dorun
      (map #(send % update-totals fields data-ref finished)
           agents))
     (block-to-done finished (count agents))
     (get-results agents fields))))

(main data-file)




;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;

; [a & b] in a let binding represent a first-rest relationship, try below:

(let [[ss & s] [1 2 3]]
  ss)


















