(ns Miscellaneous.warehouse
  (:refer-clojure :exclude [sort find])
  (:require [clojure.string :as string]
            [incanter.core :as incanter]
            [data-mall.connectDB3 :as db]
            [clojure.java.jdbc :as jdbc]
            [monger.core :as mg];the following 4 is for mongo use
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.query :refer :all]
            [Miscellaneous.purui :as purui])
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern];the following two is for mongo use
           org.bson.types.ObjectId))

(mg/connect! {:host "192.168.3.53" :port 7017})

(mg/set-db! (mg/get-db "test"))

(declare extract-text)

(defn integrate-text
  [& {:as source}]
  (let [m [:tianya :tieba :weibo :douban :youku]
        s (set (keys source))
        job (filter s m)]
    (mapcat #(extract-text % (get source %)) job)))

(integrate-text :tianya 3 :douban 2 :tieba "star_baidutieba_contents")

(defn extract-text
  [source-key source-address]
  (cond (= source-key :tianya) (println "This is tianya")
        (= source-key :tieba) (mc/find-maps source-address)
        (= source-key :weibo) (println "This is weibo")
        (= source-key :douban) (println "This is douban")
        (= source-key :youku) (println "This is youku")))

(defn extract-tianya
  [source-address]
  (let [m (mc/find-maps source-address)])
  (select-keys)
  )





(mc/find-maps "star_baidutieba_contents")

#_(with-collection "star_baidunews_history"
  find{})






