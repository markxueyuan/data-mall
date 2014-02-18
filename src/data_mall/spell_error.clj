(ns data-mall.spell-error
  (:require [clojure.string :as string])
  (:use [clojure.set :only (union)]))

(defn words
  [text]
  (re-seq #"[a-z]+" (string/lower-case text)))


(defn train
  [feats] (frequencies feats))

(def n-words
  (train (words (slurp "D:/data/big.txt"))))

(def n-words-2
  (train (words (slurp "E:/data/big.txt"))))

(def alphabet "abcdefghijklmnopqrstuvwxyz")

(defn split-word
  [word i]
  [(.substring word 0 i) (.substring word i)])

(defn delete-char
  [[w1 w2]]
  (str w1 (.substring w2 1)))

(defn transpose-split
  [[w1 w2]]
  (str w1 (second w2) (first w2) (.substring w2 2)))

(defn replace-split
  [[w1 w2]]
  (let [w2-0 (.substring w2 1)]
    (map #(str w1 % w2-0) alphabet)))

(defn edits-1
  [word]
  (let [splits (map (partial split-word word) (range (inc (count word))))
        long-splits (filter #(> (count (second %)) 1) splits)
        deletes (map delete-char long-splits)
        transposes (map transpose-split long-splits)
        ]
    )
  )

;(words "Mama takes good care of me.")


