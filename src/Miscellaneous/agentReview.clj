(ns Miscellaneous.agentReview)

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

(defn main
  ([data-file] (main data-file [:P035001 :HU100] 5 5))
  ([data-file fields agent-count chunk-count]
   (let [mzero (mapv (constantly 0) fields)
         agents (map agent
                     (take agent-count
                           (repeat mzero)))]
     (dorun
      (->>
       (lazy-read-csv data-file)
       with-header
       (partition-all chunk-count)
       (map #(send %1 sum-items fields %2)
            (cycle agents))))
     (->> agents
          (map force-value)
          (reduce accum-sums mzero)
          div-vec))))


(main data-file)


;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;The little difference between mapv and map

(map + [1 2 3] [4 5 6] [5 8])

(mapv + [1 2 3] [4 5 6] [5 8])


;compare atom and agent,  synchonicity and asynchronicity
;agent is asynchronous, so it returns right after you send it an action, no matter how long the action will actually take.
;in contrast, atom only returns after the callings finally returns.
;however, agent can block like atom by using function: await.

(def c (agent 20000))
(def b (atom 20000))
(send c #(Thread/sleep %))
(swap! b #(Thread/sleep %))
@c
@b
(future (swap! b #(Thread/sleep %)))
@b
(await c)


;ns-unmap do works, sometimes you may need it!

(ns-unmap *ns* 'data-file)



;;;;;;;;;;;;;;;not that smart;;;;;;;;;;;;;;;;;;;;;;;

;maybe one day you would figure out what the following code means:

(defn relay [x i]
  (when (:next x)
    (send (:next x) relay i))
  (when (and (zero? i) (:report-queue x))
    (.put (:report-queue x) i))
  x)

(defn run [m n]
  (let [q (new java.util.concurrent.SynchronousQueue)
        hd (reduce (fn [next _] (agent {:next next}))
                   (agent {:report-queue q}) (range (dec m)))]
    (doseq [i (reverse (range n))]
      (send hd relay i))
    (.take q)))

;(time (run 1000 1000))












