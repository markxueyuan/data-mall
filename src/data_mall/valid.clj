(ns data-mall.valid
  (:require [valip.core :as v]
            [valip.predicates :as p]
            [validateur.validation :refer :all]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;valip;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def user
  {:given-name "Fox"
   :surname "Mulder"
   :age 51
   :badge "JTT047101111"})

(defn number-present?
  [x]
  (and (p/present? (str x))
       (or (instance? Integer x)
           (instance? Long x))))

(defn valid-badge?
  [x]
  (not (nil? (re-seq #"[A-Z]{3}\d+" x))))

;(valid-badge? "ABC1234567")

(defn validate-user
  [user]
  (v/validate user
              [:given-name p/present? "麻痹告诉我你姓什么啊？！"]
              [:surname p/present? "麻痹告诉我你叫什么啊？！"]
              [:age number-present? "小妞你究竟几岁啊？！"]
              [:age (p/over 0) "年龄不可以是负的哟！"]
              [:age (p/under 150) "哇，快来看妖精！"]
              [:badge p/present? "为了联盟，出示徽章！"]
              [:badge valid-badge? "兄弟徽章不对啊！"]
  ))

;(validate-user user)
;(validate-user (assoc user :badge "XXXXOOOOO"))
;(validate-user (assoc user :surname nil))
;(validate-user (assoc user :age -23))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;validateur;;;;;;;;;;;;;;;;;;;;;;

(let [v (validation-set
         (presence-of [:name :last])
         (presence-of :age))
      result [(v {:name {:first "Joe" :last "wang"} :age 28}) (v {:name "jimmy" :age 28})]
      report (map valid? result)]
  report)
;;;;;;;;;;;;;tips;;;;;;;;;;;;;;

(instance? Integer (Integer. 3))
(instance? Long 3)
(instance? Integer 3)

;There are more predicts in valip predict namespace

