(ns data-mall.removeDupli
  (:require [clj-diff.core :as diff]))

(def fuzzy-max-diff 2)
(def fuzzy-percent-diff 0.319)
(def fuzzy-dist diff/edit-distance)


(defn fuzzy=
  "This returns a fuzzy match."
  [a b]
  (let [dist (fuzzy-dist a b)]
    (or (<= dist fuzzy-max-diff)
        (<= (/ dist (min (count a) (count b)))
            fuzzy-percent-diff)))
  )

(defn records-match
  [key-fn a b]
  (let [kfns (if (sequential? key-fn) key-fn [key-fn])
        rfn (fn [prev next-fn]
              (and prev (fuzzy= (next-fn a)
                                (next-fn b))))]
    (reduce rfn true kfns)))

(def data
  {:mulder {:given-name "Fox" :surname "Mulder"}
   :molder {:given-name "Fox" :surname "Molder"}
   :mulder2 {:given-name "fox" :surname "mulder"}
   :scully {:given-name "Dana" :surname "Scully"}
   :scully2 {:given-name "Dan" :surname "Scully"}})

(records-match [:given-name :surname] (data :mulder) (data :scully))


(records-match  identity "#独家快报#【林左鸣到成飞成都所检查指导工作】1月7日，中航工业董事长林左鸣来到成飞科研生产现场，详细察看了型号研制进展情况，为刚刚荣获成飞“每周一星”、“党员先锋岗”和“优秀青年突击队”的代表颁奖授牌，鼓励他们新年再立新功。http://t.cn/8F7rhuR" "林左鸣视察成飞。亮点——详细查看型号研制进展情况。[哈哈]
◆
◆
@中国航空报社
#独家快报#【林左鸣到成飞成都所检查指导工作】1月7日，中航工业董事长林左鸣来到成飞科研生产现场，详细察看了型号研制进展情况，为刚刚荣获成飞“每周一星”、“党员先锋岗”和“优秀青年突击队”的代表颁奖授牌，鼓励他们新年再立新功。http://t.cn/8F7rhuR"
                )



;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;

;you can count a string:

(count "毛主席万岁！")


