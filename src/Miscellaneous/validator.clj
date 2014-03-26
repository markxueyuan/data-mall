(ns Miscellaneous.validator
  (:require [clojure.java.io :as javaio]
            [clojure.data.csv :as csv]
            [incanter.io :as io]
            [incanter.core :as incanter]))

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

(defn int?
  [x]
  (or (instance? Integer x) (instance? Long x)))

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
  [rows caster sink]
  (when-let [[item & items] (seq rows)]
    (send caster coerce-row item sink)
    (send *agent* read-row caster sink)
    items))

(defn int-val?
  [x]
  (or (int? x) (empty? x)))

(defn validate
  [row]
  (or (nil? row)
      (reduce #(and %1 (int-val? (%2 row)))
              true  int-rows)))

(defn agent-ints
  [input-file]
  (let [reader (agent (seque (with-header
                               (lazy-read-csv input-file))))
        caster (agent nil)
        sink (agent [])]
    (set-validator! caster validate)
    (send reader read-row caster sink)
    {:reader reader
     :caster caster
     :sink sink}))

;(agent-ints data-file)

(empty? "")








;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;

;read-string is strange!
(read-string "1.1")
(read-string "(+ 1 1)")
#_(read-string "1.1.1 (+ 1 1)")
#_(read-string "")
(read-string "+ 2 3")

;if don't print in the catch, error will be catched silently!
(defn n [] (/ 1 0))
(try (n) (catch Exception e n))
(try (n) (catch Exception e (println n)))

;you should look at apply in a more flexible way!
(apply assoc {} (concat [:a 2] [:b 3]))
(apply (partial assoc {}) (concat [:a 2] [:b 3]))

;seque

;Creates a queued seq on another (presumably lazy) seq s. The queued
;seq will produce a concrete seq in the background, and can get up to
;n items ahead of the consumer. n-or-q can be an integer n buffer
;size, or an instance of java.util.concurrent BlockingQueue. Note
;that reading from a seque can block if the reader gets ahead of the
;producer.


;what is "" ?

(nil? "")
(empty? "") ;is the same as:
(not (seq ""))


