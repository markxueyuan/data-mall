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






