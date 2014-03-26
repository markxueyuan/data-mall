(ns Miscellaneous.watcher
  (:require [clojure.java.io :as javaio]
            [clojure.data.csv :as csv]
            [incanter.io :as io]))

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

(def int-rows
  [:GEOID :SUMLEV :STATE :POP100 :HU100 :POP100.2000
   :HU100.2000 :P035001 :P035001.2000])


(defn try-read-string
  [x]
  (try (read-string x)
    (catch Exception e x)))

(defn coerce-row
  [_ row sink]
  (let [cast-row (apply assoc row (mapcat (fn [k] [k (try-read-string (k row))]) int-rows))]
    (send sink conj cast-row)
    cast-row))

(defn read-row
  [rows caster sink done]
  (if-let [[item & items] (seq rows)]
    (do
      (send caster coerce-row item sink)
      (send *agent* read-row caster sink done)
      items)
    (do
      (dosync (commute done (constantly true)))
      '();what does this hell mean?
      )))

(defn watch-caster
  [counter watch-key watch-agent old-state new-state]
  (when-not (nil? new-state))
  (dosync (commute counter inc)))

(defn wait-for-it
  [sleep-for ref-var]
  (loop []
    (when-not @ref-var
      (Thread/sleep sleep-for)
      (recur))))

(defn watch-processing
  [input-file]
  (let [reader (agent (seq (with-header
                             (lazy-read-csv input-file))))
        caster (agent nil)
        sink (agent [])
        counter (ref 0)
        done (ref false)]
    (add-watch caster :counter (partial watch-caster counter))
    (send reader read-row caster sink done)
    (wait-for-it 250 done)
    {:results @sink
     :count-watcher @counter}))

(:count-watcher (watch-processing data-file))







;;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;

(when true 'a)
(when false 'a)
(when-not true 'a)
(when-not false 'a)















