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
            [data-mall.ansj-seg :as seg])
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern];the following two is for mongo use
           org.bson.types.ObjectId))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;extract text ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(mg/connect! {:host "192.168.3.53" :port 7017})

#_(mg/connect!)

(mg/set-db! (mg/get-db "test"))

#_(mg/set-db! (mg/get-db "star"))

(declare extract-text extract-tieba extract-tianya extract-weibo extract-douban extract-youku)

(defn insert-by-part
  [collection data]
  (let [parts (partition-all 500 data)]
    (map #(mc/insert-batch collection %) parts)))

(defn integrate-text
  [& {:as source}]
  (let [m [:tianya :tieba :weibo :douban :youku]
        s (set (keys source))
        job (filter s m)]
    (mapcat #(extract-text % (get source %)) job)))

(insert-by-part "xuetest" (integrate-text :tianya "star_tianya_content"
                                          :douban "star_douban_shortcomments"
                                          :tieba "star_baidutieba_contents"
                                          :gada "haha"
                                          :weibo "star_weibo_history"
                                          :youku "star_youku_video"))


(defn extract-text
  [source-key source-address]
  (cond (= source-key :tianya) (extract-tianya source-address)
        (= source-key :tieba) (extract-tieba source-address)
        (= source-key :weibo) (extract-weibo source-address)
        (= source-key :douban) (extract-douban source-address)
        (= source-key :youku) (extract-youku source-address)))

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

(defn extract-douban
  [source-address]
  (let [m (mc/find-maps source-address)
        f #(select-keys % [:_id :comment])
        g #(assoc {} :mid (:_id %) :text (:comment %) :level (Integer. 0) :source "douban")]
    (->> m
         (map f)
         (map g))))

(defn extract-youku
  [source-address]
  (let [m (mc/find-maps source-address)
        f #(select-keys % [:_id :text])]
    (map #(assoc % :level (Integer. 0) :source "youku") (map f m))
  ))



#_(with-collection "star_baidunews_history"
  find{})


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;word-count;;;;;;;;;;;;;;;;;;;;;;;

(defn unwind
  [entry]
  (let [pivot (dissoc entry :wordseg) ])

(defn word-count
  [collections target-key & kws]
  (->> collections
       (map #(seg/word-seg target-key %))
       (map #(select-keys % (conj kws :word-seg)))
       ))

(word-count (mc/find-maps "xuetest") :text :source)

(->> (mc/find-maps "xuetest")
     (map #(seg/word-seg :text %))
     (insert-by-part "wordseg"))

;;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;;;;

(flatten [[{:a 2} {:b 3}] [{:c 4} {:d 5}]])

