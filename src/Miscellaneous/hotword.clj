(ns Miscellaneous.hotword
  (:refer-clojure :exclude [find])
  (:require [clojure.string :as string]
            [incanter.core :as incanter]
            [data-mall.connectDB3 :as db]
            [clojure.java.jdbc :as jdbc]
            [monger.core :as mg];the following 4 is for mongo use
            [monger.collection :as mc]
            [monger.operators :refer :all]
            ;[monger.query :refer :all]
            [monger.joda-time :as mjt]
            [monger.multi.collection :as mmc]
            [data-mall.ansj-seg :as seg]
            [data-mall.synonym :as syn]
            [data-mall.pivot-table :as pt]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as joda]
            [clj-time.local :as l]
            [clojure.string :as string]
            [data-mall.moving-average :as mv]
            )
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern];the following two is for mongo use
           org.bson.types.ObjectId)
  (:use clj-excel.core))

(mg/connect! {:host "192.168.1.184" :port 7017})

(mg/set-db! (mg/get-db "xuetest"))

(defn insert-by-part
  [collection data]
  (let [parts (partition-all 500 data)]
    (map #(mc/insert-batch collection %) parts)))

(defn heavy-words
  [collection nature day amounts]
  (mc/ensure-index collection {:word 1 :nature 1 :date 1})
  (let [day (t/from-time-zone (apply t/date-time day) (t/time-zone-for-offset +8))
        next-day (t/plus (t/from-time-zone day (t/time-zone-for-offset +8)) (t/days 1))
        results (mc/aggregate collection [{$match {:pubdate {$gte day
                                                          $lt next-day}
                                                   :nature nature}}
                                          {$group {:_id {:word "$word"}
                                                   :counts {$sum 1}}}
                                          {$sort {:counts -1}}
                                          {$limit amounts}])
        pairs (map #(vector ((comp :word :_id) %) (:counts %)) results)]
    (reduce #(apply (partial assoc %1) %2) {} pairs)))

(defn previous-average
  [collection word nature day]
  (let [previous-day (t/plus (t/from-time-zone day (t/time-zone-for-offset +8)) (t/days -7))
        previous-sum (->> (mc/aggregate collection [{$match {:word word
                                                            :pubdate {$gte previous-day
                                                                   $lt day}
                                                            :nature nature}}
                                                   {$group {:_id {:word "$word"}
                                                            :counts {$sum 1}}}
                                                   {$project {:_id 0
                                                              :counts 1}}])
                          first
                          :counts)]
    (if (nil? previous-sum)
      0.1
      (double (/ previous-sum 7)))))


;(heavy-words "mahang_segs" "名词" [2014 3 15] 500)

;(heavy-words "mahang_segs_weibo" "名词" [2014 3 15] 500)
;(previous-average "mahang_segs" "酒店" "名词" (t/from-time-zone (apply t/date-time [2014 3 19]) (t/time-zone-for-offset +8)))

(defn hot-words
  [collection nature day amounts]
  (let [heavy (heavy-words collection nature day amounts)
        day (t/from-time-zone (apply t/date-time day) (t/time-zone-for-offset +8))
        func #(previous-average collection % nature day)]
    (->> (reduce #(assoc %1 (key %2) (/ (val %2) (func (key %2)))) {} heavy)
         (sort #(> (val %1) (val %2))))))

;(hot-words "mahang_segs" "动词" [2014 3 11] 100)
;(sort #(> (val %1) (val %2)) {:b 1 :c 3 :a  2})


(defn print-hot-word
  [collection day-range amounts]
  (let [f #(->> (hot-words collection %1 %2 amounts)
                (take 50)
                (map first))
        n #(f "名词" %)
        a #(f "形容词" %)
        v #(f "动词" %)
        s #(string/join %1 %2)]
    (reduce #(conj %1 (vector (s "-" %2)
                              (s " " (n %2))
                              (s " " (a %2))
                              (s " " (v %2))))
            [["日期" "名词热词" "形容词热词" "动词热词"]]
         day-range)))

(def day-range
  [
[2014 	3 	1]
[2014 	3 	2]
[2014 	3 	3]
[2014 	3 	4]
[2014 	3 	5]
[2014 	3 	6]
[2014 	3 	7]
[2014 	3 	8]
[2014 	3 	9]
[2014 	3 	10]
[2014 	3 	11]
[2014 	3 	12]
[2014 	3 	13]
[2014 	3 	14]
[2014 	3 	15]
[2014 	3 	16]
[2014 	3 	17]
[2014 	3 	18]
[2014 	3 	19]
[2014 	3 	20]
[2014 	3 	21]
[2014 	3 	22]
[2014 	3 	23]
[2014 	3 	24]
[2014 	3 	25]
[2014 	3 	26]
[2014 	3 	27]
[2014 	3 	28]
[2014 	3 	29]
[2014 	3 	30]
[2014 	3 	31]
[2014 	4 	1]
[2014 	4 	2]
[2014 	4 	3]
[2014 	4 	4]
[2014 	4 	5]
[2014 	4 	6]
[2014 	4 	7]
[2014 	4 	8]
[2014 	4 	9]
[2014 	4 	10]])

day-range

(->> {"热词" (print-hot-word "mahang_segs" day-range 100)}
     (build-workbook (workbook-xssf))
     (#(save % "D:/data/baidunewshotword.xlsx")))

(->> {"热词" (print-hot-word "mahang_segs_weibo" day-range 100)}
     (build-workbook (workbook-xssf))
     (#(save % "D:/data/mahang/weibohotword.xlsx")))

;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;


(string/join "-" [2012 2 3])

