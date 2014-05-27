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

(mg/set-db! (mg/get-db "lightdata"))

(defn insert-by-part
  [collection data]
  (let [parts (partition-all 500 data)]
    (map #(mc/insert-batch collection %) parts)))

(defn heavy-words
  [collection nature day amounts]
  (mc/ensure-index collection {:word 1 :nature 1 :pubdate 1})
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
  [collection word nature day back]
  (let [previous-day (t/plus (t/from-time-zone day (t/time-zone-for-offset +8)) (t/days (- 0 back)))
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
      (double (/ previous-sum back)))))


;(heavy-words "mahang_segs" "名词" [2014 3 15] 500)

;(heavy-words "mahang_segs_weibo" "名词" [2014 3 15] 500)
;(previous-average "mahang_segs" "酒店" "名词" (t/from-time-zone (apply t/date-time [2014 3 19]) (t/time-zone-for-offset +8)))

(defn hot-words
  [collection nature day amounts back]
  (let [heavy (heavy-words collection nature day amounts)
        day (t/from-time-zone (apply t/date-time day) (t/time-zone-for-offset +8))
        func #(previous-average collection % nature day back)]
    (->> (reduce #(assoc %1 (key %2) (/ (val %2) (func (key %2)))) {} heavy)
         (sort #(> (val %1) (val %2))))))

;(hot-words "mahang_segs" "动词" [2014 3 11] 100)
;(sort #(> (val %1) (val %2)) {:b 1 :c 3 :a  2})


(defn print-hot-word
  [collection day-range amounts back]
  (let [f #(->> (hot-words collection %1 %2 amounts back)
                (take 50)
                (map first))
        n #(f "名词" %)
        na #(f "人名" %)
        sn #(f "专有名词" %)
        a #(f "形容词" %)
        v #(f "动词" %)
        s #(string/join %1 %2)]
    (reduce #(conj %1 (vector (s "-" %2)
                              (s " " (n %2))
                              (s " " (na %2))
                              (s " " (sn %2))
                              (s " " (a %2))
                              (s " " (v %2))))
            [["日期" "名词热词" "人名热词" "专有名词热词" "形容词热词" "动词热词"]]
         day-range)))



(defn make-day-range
  [start end]
  (let [time-fn #(t/from-time-zone (apply t/date-time %) (t/time-zone-for-offset +8))
        to-day #(vector (t/year %) (t/month %) (t/day %))
        start-time (time-fn start)
        end-time (time-fn end)
        a (atom [])]
    (loop [date-time start-time]
      (if-not (< 0 (compare date-time end-time))
        (do
          (swap! a #(conj % (to-day date-time)))
          (recur (t/plus date-time (t/days 1))))
        @a))))



#_(def car-dayrange
  (make-day-range [2014 4 8] [2014 5 5]))

(def game-dayrange
  (make-day-range [2014 4 1] [2014 4 30]))


#_(->> {"热词" (print-hot-word "game_weibo_segs" game-dayrange 100 3)}
     (build-workbook (workbook-xssf))
     (#(save % "D:/data/game/weibohotword.xlsx")))

(->> {"热词" (print-hot-word "game_baidunews_segs" game-dayrange 100 3)}
     (build-workbook (workbook-xssf))
     (#(save % "D:/data/game/newshotword.xlsx")))

;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;


(string/join "-" [2012 2 3])

