(ns Miscellaneous.practiceTMT
  (:require [incanter.core :as incanter]
            [incanter.io :as io]
            [clojure.string :as string]))

(def punc-regex  #"[,.\s;:-?!\/\\\"\'_\(\)\{\}\[\]@—\$&\^\*\+\t\r\n\a\e\f\v]")

;(incanter/view (io/read-dataset "E:/training/pubmed-oa-subset.csv"))


(defn tokenizer
  [text]
  (if (nil? text)
    nil
    (let [splt (string/split text punc-regex) ;split by spaces or punctures
          pred (fn [x] (>= (count x) 3))]
      (->> splt
           (remove #(= "" %)) ;remove redundant ""
           (map string/lower-case) ;turn all words to lower case
           (map #(re-seq #"[a-z0-9]" %)) ;only keep numbers or characters
           (map #(apply str %))
           ;(map #(str % " "))
           ;(apply str)
           (filter pred) ;filter out words longer than 2
           ))))

(defn read-text
  [csv-file col-name]
  (for [row (->> csv-file
                 io/read-dataset
                 :rows)]
    (let [token (->> col-name
                     keyword
                     row
                     tokenizer)]
      (assoc row (keyword col-name) token)
       )))

(defn sort-map-value
  [map-obj]
  (into (sorted-map-by (fn [key1 key2]
                         (compare [(get map-obj key2) key2]
                                  [(get map-obj key1) key1])))
        map-obj))


(defn heuristic-filter
  [csv-file col-name]
  (let [dataset (read-text csv-file col-name)]
    (->> dataset
         (map (keyword col-name))
         (apply concat)
         frequencies
         sort-map-value
         (drop 40)
         (into {})
         (filter #(> (val %) 5))
         (into {})
         keys
         set
         )))

;(heuristic-filter "D:/training/pubmed-oa-subset.csv" "col3")

;(read-text "D:/training/pubmed-oa-subset.csv" "col3")

(defn data-ready
  [csv-file col-name]
  (let [flt (heuristic-filter csv-file col-name)]
    (for [row (read-text csv-file col-name)]
      (assoc row (keyword col-name) (filter #(flt %) (get row (keyword col-name))))
    )))

;(data-ready "D:/training/pubmed-oa-subset.csv" "col3")





;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;

(string/upper-case "thk")
(string/lower-case "THK")


(remove even? [2 3 4])
(filter even? [2 3 4])




;;;please compare the following two

(let [results {:A 1 :B 2 :C 2 :D 5 :E 1 :F 1}]
  (into (sorted-map-by (fn [key1 key2]
                         (compare (get results key2)
                                  (get results key1))))
        results))



(let [results {:A 1 :B 2 :C 2 :D 5 :E 1 :F 1}]
  (into (sorted-map-by (fn [key1 key2]
                         (compare [(get results key2) key2]
                                  [(get results key1) key1])))
        results))

;;;more information please search function sorted-map-by in clojuredocs.org
