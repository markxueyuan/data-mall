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

(declare extract-text extract-tieba)

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
        (= source-key :tieba) (extract-tieba source-address)
        (= source-key :weibo) (println "This is weibo")
        (= source-key :douban) (println "This is douban")
        (= source-key :youku) (println "This is youku")))

(defn extract-tieba
  [source-address]
  (let [m (mc/find-maps source-address)
        f #(map :text (:minireps %))
        g (fn [i] (update-in i [:minireps] (partial apply str)))
        h (fn [i] [(select-keys i [:_id :minireps])
                   (select-keys i [:_id :text])])
        j (fn [i] [(assoc {} :mid (:_id (first i)) :text (:minireps (first i)) :level (Integer. 1))
                   (assoc {} :mid (:_id (second i)) :text (:text (second i)) :level (Integer. 0))])]
    (->> m
         (map #(select-keys % [:_id :text :minireps]))
         (map #(assoc % :minireps (f %)))
         (map g)
         (map h)
         (map j)
         flatten
         (remove #(= "" (:text %)))
         (map #(assoc % :source "tianya"))
         )))

(mc/insert-batch "xuetest" (extract-tieba "star_baidutieba_contents"))

(extract-tieba "star_baidutieba_contents")

((comp :text :minireps) {:minireps {:text '3}})


(mc/find-maps "star_baidutieba_contents")

#_(with-collection "star_baidunews_history"
  find{})



(flatten [[{:a 2} {:b 3}] [{:c 4} {:d 5}]])


