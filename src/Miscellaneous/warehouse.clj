(ns Miscellaneous.warehouse
  (:refer-clojure :exclude [sort find])
  (:require [clojure.string :as string]
            [incanter.core :as incanter]
            [data-mall.connectDB3 :as db]
            [clojure.java.jdbc :as jdbc]
            [monger.core :as mg];the following 4 is for mongo use
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.query :refer :all])
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern];the following two is for mongo use
           org.bson.types.ObjectId))

#_(mg/connect! {:host "192.168.3.53" :port 7017})

(mg/connect!)

#_(mg/set-db! (mg/get-db "test"))

(mg/set-db! (mg/get-db "star"))

(declare extract-text extract-tieba extract-tianya extract-weibo extract-douban extract-youku)

(defn integrate-text
  [& {:as source}]
  (let [m [:tianya :tieba :weibo :douban :youku]
        s (set (keys source))
        job (filter s m)]
    (mapcat #(extract-text % (get source %)) job)))

(mc/insert-batch "xuetest" (integrate-text :tianya "tianya" :douban 2 :tieba "tieba" :gada "haha" :weibo "weibo"))

(defn extract-text
  [source-key source-address]
  (cond (= source-key :tianya) (extract-tianya source-address)
        (= source-key :tieba) (extract-tieba source-address)
        (= source-key :weibo) (extract-weibo source-address)
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
         (map #(assoc % :source "tieba"))
         )))

(defn extract-tianya
  [source-address]
  (let [m (mc/find-maps source-address)
        f #(select-keys % [:_id :content])
        g #(assoc {} :mid (:_id %) :text (:content %) :level (Integer. 0) :source "tianya")]
    (->> m
         (map f)
         (map g)
         )))

(defn extract-weibo
  [source-address]
  (let [m (mc/find-maps source-address)
        f #(select-keys % [:_id :text])
        g #(assoc {} :mid (:_id %) :text (:text %) :level (Integer. 0) :source "weibo")]
    (->> m
         (map f)
         (map g))))

;(extract-weibo "weibo")






(mc/insert-batch "xuetest" (extract-tieba "tieba"))

;(extract-tieba "star_baidutieba_contents")






#_(with-collection "star_baidunews_history"
  find{})

;;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;;;;

(flatten [[{:a 2} {:b 3}] [{:c 4} {:d 5}]])


