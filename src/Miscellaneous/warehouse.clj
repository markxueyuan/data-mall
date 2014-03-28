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
            [data-mall.ansj-seg :as seg]
            [data-mall.synonym :as syn]
            [data-mall.pivot-table :as pt]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as joda]
            [clj-time.local :as l]
            )
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern];the following two is for mongo use
           org.bson.types.ObjectId))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;extract text ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



(declare extract-text extract-tieba extract-tianya extract-weibo extract-douban extract-youku)

(defn insert-by-part
  [collection data]
  (let [parts (partition-all 500 data)]
    (map #(mc/insert-batch collection %) parts)))

(defn integrate-text
  [{:as source}]
  (let [m [:tianya :tieba :weibo :douban :youku]
        s (set (keys source))
        job (filter s m)]
    (mapcat #(extract-text % (get source %)) job)))

#_(insert-by-part "xuetest" (integrate-text {:tianya "star_tianya_content"
                                          :douban "star_douban_shortcomments"
                                          :tieba "star_baidutieba_contents"
                                          :gada "haha"
                                          :weibo "star_weibo_history"
                                          ;:youku "star_youku_video"
                                           }))


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






;;;;;;;;;;;;;;;;;;;;;;;;;;;;;word-seg;;;;;;;;;;;;;;;;;;;;;;;

(defn unwind
  [entry]
  (let [pivot (dissoc entry :word-seg)
        word-seg (get entry :word-seg)
        func #(into pivot %)]
    (map func word-seg)))

;(unwind {:a 1 :b 2 :word-seg [{:word 4 :nature 5} {:word 6 :nature 7}]})

(defn word-seg
  [collections target-key & kws]
  (->> collections
       ;(take 5)
       (map #(seg/word-seg target-key %))
       (map #(select-keys % (conj kws :word-seg)))
       (map unwind)
       (apply concat)
       (filter #(> (count (:word %)) 1))
       (map #(syn/han :nature %))
       ;frequencies
       ;(map #(assoc (first %) :counts (second %)))
       ))

;(insert-by-part "word_count" (word-seg (mc/find-maps "xuetest") :text :source :mid))

;;;;;;;;;;;;;;;;;;;;;;;;;;date counting;;;;;;;;;;;;;;;;;;;;;;

(defn correct-nil
  [stuff col-key entry]
  (if (or (= "" (col-key entry))(nil? (col-key entry)))
    (assoc entry col-key stuff)
  entry))

(defn parse-date
  [string]
  (let [fmt (f/formatter (t/default-time-zone) "yyyy-MM-dd HH:mm:ss" "yyyy-MM-dd HH:mm" "yyyy-MM-dd")]
    (->> string
         (f/parse fmt))))

(defn extract-date
  [location {:keys [source mid] :as entry}]
  (case source
    "tianya" (->> (:tianya location)
                  (#(mc/find-one-as-map % {:_id mid} {:_id 0 :pubtimestr 1}))
                  (correct-nil "1970-1-1":pubtimestr)
                  :pubtimestr
                  parse-date)
    "douban" (->> (:douban location)
                  (#(mc/find-one-as-map % {:_id mid} {:_id 0 :pubdate 1}))
                  (correct-nil "1970-1-1":pubdate)
                  :pubdate
                  parse-date
                  )
    "tieba" (->> (:tieba location)
                 (#(mc/find-one-as-map % {:_id mid} {:_id 0 "content.date" 1}))
                 :content
                 (correct-nil "1970-1-1":date)
                 :date
                 parse-date
                 )
    "weibo" (->> (:weibo location)
                 (#(mc/find-one-as-map % {:_id mid} {:_id 0 :pubtime 1}))
                 (correct-nil 0 :date)
                 :pubtime
                 long
                 joda/from-long
                 (#(t/to-time-zone % (t/time-zone-for-offset +8)))
                 )
    ;"youku" (->> (:youku location)
                 ;(#(mc/find-one-as-map % {:_id mid} {:_id 0 :pubtime 1}))
                 ;)
    ))

;(extract-date locations {:source "weibo" :mid (ObjectId. "530ec0ad07b83b420009e8b5")})

(defn associate-date
  [location {:as entry}]
  (let [fmt (f/formatter "YYYY-MM-dd")]
    (assoc entry :pubdate (l/format-local-time (extract-date location entry) :date))))

#_(associate-date locations {:source "weibo" :mid (ObjectId. "530ec0ad07b83b420009e8b5")})



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;working zone;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(mg/connect! {:host "192.168.3.53" :port 7017})

#_(mg/connect!)

(mg/set-db! (mg/get-db "test"))

#_(mg/set-db! (mg/get-db "star"))

(def locations {:tianya "star_tianya_content"
             :douban "star_douban_shortcomments"
             :tieba "star_baidutieba_contents"
             :gada "haha"
             :weibo "star_weibo_history"
             ;:youku "star_youku_video"
               })


(defn make-all
  [locations]
  (->> locations
       integrate-text
       (map #(associate-date locations %))
       (#(word-seg % :text :source :mid :pubdate))))

(->> (make-all locations)
     (insert-by-part "xuetestall"))



;;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;;;;

(flatten [[{:a 2} {:b 3}] [{:c 4} {:d 5}]])

(f/show-formatters)

(f/formatters :basic-date-time)
