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

(defn insert-split
  [[w1 w2]]
  (map #(str w1 % w2) alphabet))

(defn edits-1
  [word]
  (let [splits (map (partial split-word word) (range (inc (count word))))
        long-splits (filter #(> (count (second %)) 1) splits)
        deletes (map delete-char long-splits)
        transposes (map transpose-split long-splits)
        replaces (mapcat replace-split long-splits)
        inserts (remove nil? (mapcat insert-split splits))] ;don't know why need a remove here
    (set (concat deletes transposes replaces inserts))))

(defn known-edits-2
  [word]
  (set (filter (partial contains? n-words)
               (apply union (map #(edits-1 %) (edits-1 word))))))

(defn known
  [words]
  (set (filter (partial contains? n-words) words)))

(defn correct
  [word]
  (let [candidate-thunks [#(known (list word))
                          #(known (edits-1 word))
                          #(known-edits-2 word)
                          #(list word)]]
    (->> candidate-thunks
         (map (fn [f] (f)))
         (filter #(> (count %) 0))
         first
         (map (fn [w] [(get n-words w 1) w]))
         (reduce (partial max-key first))
         second)))












