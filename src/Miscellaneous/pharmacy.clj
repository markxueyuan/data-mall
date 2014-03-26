(ns Miscellaneous.pharmacy
  (:refer-clojure :exclude [sort find])
  (:require [clojure.string :as string]
            [incanter.core :as incanter]
            [data-mall.connectDB3 :as db]
            [clojure.java.jdbc :as jdbc]
            [monger.core :as mg];the following 4 is for mongo use
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.query :refer :all]
            ;[data-mall.ansj-seg :as seg]
            [data-mall.synonym :as syn]
            [data-mall.pivot-table :as pt])
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern];the following two is for mongo use
           org.bson.types.ObjectId))

(defn insert-by-part
  [collection data]
  (let [parts (partition-all 500 data)]
    (map #(mc/insert-batch collection %) parts)))

(defn insert-by-part2
  [db collection data]
  (let [parts (partition-all 500 data)]
    (map #(mc/insert-batch db collection % WriteConcern/NORMAL) parts)))

(mg/connect! {:host "192.168.1.184" :port 7017})

(mg/set-db! (mg/get-db "medicine"))

(defn data [] (mc/find-maps "baidu_realtime"))


(defn insert-other-db
  [db collection data]
  (let [archive-db (mg/get-db db)]
    (insert-by-part2 archive-db collection data)))

#_(->> (data)
     (map :encrypedLink)
     (map #(assoc {} :_id %))
     distinct
     (insert-other-db "gu_chain" "xuetest")
     )



;;;;;;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;;;

(distinct [{:a 2 :b 3} {:a 2 :b 3}])



