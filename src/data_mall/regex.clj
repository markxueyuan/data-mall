(ns data-mall.regex)


(require '[clojure.string :as string])

(def phone-regex
  #"(?x)
  \d{3}
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

