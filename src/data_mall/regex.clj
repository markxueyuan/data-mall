(ns data-mall.regex)


(require '[clojure.string :as string])

(def phone-regex
  #"(?x)
  (\d{3})
  \D{0,2}
  (\d{3})
  \D?
  (\d{4})
  ")

(defn clean-us-phone
  [phone]
  (if-let [[_ area-code prefix post] (re-find phone-regex phone)]
    (str \( area-code \) prefix \- post)))


;;;;;;;;;;;not that smart;;;;;;;;;;;;;;;;
(re-find phone-regex "123- 456-7890")

(clean-us-phone "123-456-7890")

(def puncture-regex
  #"[^0-9a-zA-Z]")
(def re2 #"[0-9a-zA-Z\s]")

(re-find puncture-regex "398abAB,")

(re-seq puncture-regex "398defs")

(apply str (re-seq re2 "what is the fucking matter with you?%&*^*&(*)))OPhouhou who tu say what"))

(re-seq re2 "ab@c ")



(seq "我爱你,中国")

(def reg #"你好|啊")

(not (boolean (re-find reg "年轻人你好啊！")))
(re-seq reg "年轻人你好啊！")


(def brackets #"\[[^\[\]]+\]")
(def )



(re-seq brackets "我们仍是有希望的吗[呵呵]这可咋整[哈哈]也许是吧")
