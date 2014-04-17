(ns Miscellaneous.hotword
  (:refer-clojure :exclude [sort find])
  (:require [clojure.string :as string]
            [incanter.core :as incanter]
            [data-mall.connectDB3 :as db]
            [clojure.java.jdbc :as jdbc]
            [monger.core :as mg];the following 4 is for mongo use
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.query :refer :all]
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
        results (mc/aggregate collection [{$match {:date {$gte day
                                           $lt next-day}
                                                   :nature nature}}
                                          {$group {:_id {:word "$word"}
                                                   :counts {$sum 1}}}
                                          {$sort {:counts -1}}])
        func #()]
))


(heavy-words "mahang_segs" "形容词" [2014 4 5] 500)


