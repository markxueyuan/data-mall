(ns data-mall.ansj-seg
  (:import (java.util List)
           (org.ansj.domain Term)
           (org.ansj.splitWord.analysis BaseAnalysis)
           (org.ansj.splitWord.analysis ToAnalysis)
           (org.ansj.recognition NatureRecognition)
           ))


;;;;;;;;;;;;;;;;;;fn;;;;;;;;;;;;;;;;;;;;;;;;;

(defn mapper
  "looted from Hu's code"
  [^Term item]
  {:word (.getName item)
   :nature (.natureStr (.getNatrue item))})

(defn factory
  "basically copied from Hu"
  [string]
  (let [terms (ToAnalysis/parse string)
        recog (new NatureRecognition terms)
        _ (.recognition recog)]
    terms
  ))

(defn word-seg
  [col-key entry]
  (let [seg (factory (col-key entry))
        mapped (map mapper seg)]
    (into entry {:word-seg mapped})))

(word-seg :c {:a 12345 :b "hahahah" :c "我作为一个中国人是无比自豪的！"})





;(word-seg :a {:a "这个很有用奥"})
