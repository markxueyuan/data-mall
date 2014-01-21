(ns data-mall.normalizeNumber
  (:require [clojure.string :as string]))

(defn normalize-number
  [n]
  (let [v (string/split n #"[,.]")
        [pre post] (split-at (dec (count v)) v)]
    (Double/parseDouble (apply str (concat pre [\.] post)))))



;;;;;;;;;;;;;;;;;call;;;;;;;;;;;;;;;;

(normalize-number "1,000.0")

(normalize-number "1.000,0")

(normalize-number "3,14159")



;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;;



; function take and drop constitutes split-at
(take 2 [1 2 3])
(drop 2 [1 2 3])
(split-at 2 [1 2 3])

; concat works on collections,apply works on elements of a collection
(concat [1 2] [3 4 5] #{6})
(apply str (concat [1 2] [3 4 5] #{6}))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;not that smart;;;;;;;;;;;;;;;;;;;;;;;;




