(ns Miscellaneous.practiceTMT
  (:require [incanter.core :as incanter]
            [incanter.io :as io]))


;;import the CSV data, turn it to a dataset of incanter and read the lines of it.

(->>
 (io/read-dataset "D:/training/pubmed-oa-subset.csv")
 :rows)


(seq "hahahah")

