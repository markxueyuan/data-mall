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

(def alphabet "abcdefghijklmnopqrstuvwxyz")

(defn split-word
  [word i]
  [(.substring word 0 i) (.substring word i)])

(defn delete-char
  [[w1 w2]]
  (str w1 (.substring w2 1)))



;(words "Mama takes good care of me.")
