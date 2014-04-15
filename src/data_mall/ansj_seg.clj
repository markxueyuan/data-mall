(ns data-mall.ansj-seg
  (:import (java.util List)
           (org.ansj.domain Term)
           (org.ansj.splitWord.analysis BaseAnalysis)
           (org.ansj.splitWord.analysis ToAnalysis)
           (org.ansj.recognition NatureRecognition)
           (org.ansj.library UserDefineLibrary)
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

;(word-seg :c {:a 12345 :b "hahahah" :c "我作为一个中国人是无比自豪的！"})

(defn define-library
  [white-list]
  (for [x white-list]
    (let [[word nature frequency] x]
      (UserDefineLibrary/insertWord word nature frequency))))


(def white-list [["沙夫豪森" "selfdefined" 1000
                  "超薄机芯" "selfdefined" 1000
                  "潜水表" "selfdefined" 1000
                  "镂空" "selfdefined" 1000
                  "陀飞轮" "selfdefined" 1000
                  "超长动力" "selfdefined" 1000
                  "动物灵感" "selfdefined" 1000
                  "三问表" "selfdefined" 1000
                  "月相" "selfdefined" 1000
                  "两地时" "selfdefined" 1000
                  "计时码表" "selfdefined" 1000]])


(defn factory-with-dic
  [string]
  (let [_ (define-library white-list)
        terms (ToAnalysis/parse string)
        recog (new NatureRecognition terms)
        _ (.recognition recog)]
    terms))

;(word-seg :a {:a "这个很有用奥"})

(time (factory-with-dic "陀飞轮是一个好东西"))

