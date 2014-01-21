(ns Miscellaneous.practiceTMT
  (:require [incanter.core :as incanter]
            [incanter.io :as io]
            [clojure.string :as string]))


;;import the CSV data, turn it to a dataset of incanter and read the lines of it.

(def bb
  (->>
 (io/read-dataset "D:/training/pubmed-oa-subset.csv")
 :rows
 first
 :col3))

(def punc-regex  #"[,.\s;:-?!\/\\\"\'_\(\)\{\}\[\]@—\$&\^\*\+\t\r\n\a\e\f\v]")

(incanter/view (io/read-dataset "D:/training/pubmed-oa-subset.csv"))


(seq "hahahah")

(string/split "ha,hahah.gaga,wa_wa (go)by{crazy}is[tit]3@4—mama$100&fa
              fa#"
             punc-regex)

(def sstt "ha,hahah.gaga,wa_wa(go)by{CGrazy}is[tit]3@4—mama$100&fa
             fa#")

(str " " "a")

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

(def aa (read-text "D:/training/pubmed-oa-subset.csv" "col3"))

(frequencies (apply concat (map :col3 aa)))

(sort-map-by)


(defn document-count-filter
  )




(defn xxx
  [x]
  (if (nil? x)
    3
    4))


(tokenizer sstt)
(tokenizer bb)

(string/lower-case "fafa#")


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;

(string/upper-case "thk")
(string/lower-case "THK")

(#(string/split % punc-regex) sstt)

(remove even? [2 3 4])

hash-map
(apply assoc {} [3 5 7 9 8 10])

(apply assoc {} 3 4 5 60)

(assoc {:3 4} :3 5)




