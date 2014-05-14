(ns Miscellaneous.newware
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
            [clojure.java.jdbc :as jdbc]
            )
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern];the following two is for mongo use
           org.bson.types.ObjectId)
  (:use clj-excel.core))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;extract text ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



(defn insert-by-part
  [collection data]
  (let [parts (partition-all 500 data)]
    (map #(mc/insert-batch collection %) parts)))



;(mg/connect! {:host "192.168.1.184" :port 7017})

;(mg/connect!)
;(mg/set-db! (mg/get-db "xuetest"))

(defn correct-nil
  [string]
  (if (or (= "" string) (nil? string))
    "1970-1-1"
    string))


(defn parse-date
  [string]
  (let [fmt (f/formatter (t/default-time-zone) "yyyy-MM-dd HH:mm:ss" "yyyy-MM-dd HH:mm" "yyyy-MM-dd" "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")]
    (->> (correct-nil string)
         (f/parse fmt))))

(defn unparse-date
  [time]
  (let [fmt (f/formatter (t/default-time-zone) "yyyy-MM-dd HH:mm:ss" "yyyy-MM-dd HH:mm" "yyyy-MM-dd" "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")]
    (f/unparse fmt time)))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;link main;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-key
  [from-col to-col from-key to-key]
  (mc/ensure-index from-col {from-key 1})
  (let [col (mc/find-maps to-col)]
    (for [entry col]
      (let [match (mc/find-one-as-map from-col {from-key (get entry to-key)})]
        (assoc entry :keyword (:keywords match))))))

(defn get-key-p5
  [from-col to-col from-key to-key _]
  (mc/ensure-index from-col {from-key 1})
  (let [col (mc/find-maps to-col)]
    (for [entry col]
      (let [match (mc/find-one-as-map from-col {from-key (get entry to-key)})]
        (assoc entry :keyword (:keywords match) :p5 (:p5 match))))))

(defn get-main-text
  [from-col to-col from-key to-key]
  (mc/ensure-index from-col {from-key 1})
  (let [col (mc/find-maps to-col)]
    (for [entry col]
      (let [match (mc/find-one-as-map from-col {from-key (get entry to-key)})]
        (assoc entry :extracted (apply str (:extracted match)))
        ))))


;(insert-by-part "xuexuetest" (get-main-text "mahang_baidunews_content" "mahang_news_items" :url :cache))



(defn write-key-db
  [links]
  (for [link links]
    (let [[result-db & origin] link]
      (->> (apply get-key origin)
           (insert-by-part result-db)))))

#_(write-key-db [["xuetesttieba" "baidu_tieba_main" "baidu_tieba_contents" :url :url]
               ["xuetesttianya" "tianya_search" "tianya_content" :url :url]])


(defn read-data
  [collection]
  (mc/find-maps collection))


(defn extract-tieba
  [entry]
  (let [mini (:minireps entry)
        fuser #(str (:user_name %))
        fdate #(parse-date (:time %))
        level 3
        ftext #(:text %)
        fmap #(assoc {} :user (fuser %) :pubdate (fdate %) :level level :text (ftext %))
        minimaps (map fmap mini)
        mlevel (if (> (:floor (:content entry)) 1) 2 1)
        majormap {:user (:name (:author entry)) :pubdate (parse-date (:postTime entry)) :level mlevel :text (:text entry)}
        allmaps (conj minimaps majormap)
        idmaps (map #(assoc % :_id (ObjectId.) :mid (:_id entry) :keyword (:keyword entry) :source "tieba" :title (:title entry) :url (:url entry))
                    allmaps)]
    idmaps
    ))

;(insert-by-part "xuetesttiebaextract"(apply concat (map #(extract-tieba %) (mc/find-maps "xuetesttieba"))))


(defn extract-tianya
  [entry]
  (let [user (:author entry)
        date (parse-date (:pubtime entry))
        level (if (> (:floor entry) 0) 2 1)
        text (:content entry)
        mid (:_id entry)
        kw (:keyword entry)
        source "tianya"
        title (:title entry)
        url (:url entry)]
    {:user user :pubdate date :level level :text text :_id (ObjectId.) :mid mid :keyword kw :source source :title title :url url}))

(defn extract-baidu-tianya
  [entry]
  (let [user (:author entry)
        date (parse-date (:pubtime entry))
        level (if (> (:floor entry) 0) 2 1)
        text (:content entry)
        mid (:_id entry)
        kw (:keyword entry)
        source "tianya"
        title (:title entry)
        url (:url entry)
        p5 (:p5 entry)]
    {:user user :pubdate date :level level :text text :_id (ObjectId.) :mid mid :keyword kw :source source :title title :url url :p5 p5}))

;(insert-by-part "xuetesttianyaextract" (map #(extract-tianya %) (mc/find-maps "xuetesttianya")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;extract facial expression;;;;;;;;;;;;;;;;;;;;;;;;;

(defn extract-face
  [entry]
  (->> entry
      :text
      (re-seq #"\[[^\[\]]+\]")))

(defn extract-weibo
  [entry]
  (let [user (:userName entry)
        date (->> (:pubtime entry)
                  ((fn [t] (if (nil? t) 0 t)))
                  long
                  joda/from-long
                  (#(t/to-time-zone % (t/time-zone-for-offset +8))))
        level (if (nil? (:origPostUrl entry)) 1 2)
        text (:text entry)
        mid (:_id entry)
        kw (:keyword (:opts entry))
        source "weibo"
        original (:origPostUrl entry)
        userid (:userId entry)
        url (:weiboUrl entry)
        face (extract-face entry)]
    {:user user :pubdate date :level level :text text :_id (ObjectId.) :mid mid
     :keyword kw :source source :origPostUrl original :userId userid :url url :face face}))


(defn extract-douban
  [entry])

(defn extract-youku
  [entry])

(defn extract-baidunews
  [entry]
  (let [origin (:origin entry)
        date (parse-date (:pubtime entry))
        title (:title entry)
        similar (:similar_count entry)
        kw ((comp :keyword :opts) entry)
        preview (:preview entry)
        text (:extracted entry)
        source "baidunews"]
    {:origin origin :pubdate date :title title :similar similar :keyword kw :preview preview :text text :source source}))



(defn prepare-data
  ([extract-fn from-col to-col from-key to-key]
   (->> (get-key from-col to-col from-key to-key)
        (map extract-fn)))
  ([extract-fn col]
   (map extract-fn (read-data col)))
  ([extract-fn from-col to-col from-key to-key _]
   (->> (get-key-p5 from-col to-col from-key to-key _)
        (map extract-fn))))

(defn prepare-baidunews
  ([from-col to-col from-key to-key]
   (->> (get-main-text from-col to-col from-key to-key)
        (map extract-baidunews))))

;(insert-by-part "xuexuetest" (prepare-baidunews "mahang_baidunews_content" "mahang_news_items" :url :cache))

;(prepare-data extract-weibo "weibo_history")

;(prepare-data extract-tieba "baidu_tieba_main" "baidu_tieba_contents" :url :url)


(defn utility
  [source-key source-docs]
  (cond (= source-key :tianya) (apply (partial prepare-data extract-tianya) source-docs)
        (= source-key :tieba) (apply concat (apply (partial prepare-data extract-tieba) source-docs))
        (= source-key :weibo) (apply (partial prepare-data extract-weibo) source-docs)
        (= source-key :douban) (apply (partial prepare-data extract-douban) source-docs)
        (= source-key :youku) (apply (partial prepare-data extract-youku) source-docs)
        (= source-key :baidu-tianya) (apply (partial prepare-data extract-baidu-tianya) source-docs)
        (= source-key :baidu-news) (apply prepare-baidunews source-docs)))


(defn integrate
  [{:as source}]
  (let [m [:tianya :tieba :weibo :douban :youku :baidu-tianya :baidu-news]
        s (set (keys source))
        job (filter s m)]
    (mapcat #(utility % (get source %)) job)))




(def source {:weibo ["weibo_history"]
             :tianya ["tianya_search" "tianya_content" :url :url]
             :tieba ["baidu_tieba_main" "baidu_tieba_contents" :url :url]
             :haha []})

;(insert-by-part "xuetestintegrate" (integrate source))

;(insert-by-part "xuexuetest" (integrate {:baidu-news ["mahang_baidunews_content" "mahang_news_items" :url :cache]}))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;word-seg;;;;;;;;;;;;;;;;;;;;;;;

(defn unwind
  [entry]
  (let [pivot (dissoc entry :word-seg)
        word-seg (get entry :word-seg)
        func #(into pivot %)]
    (map func word-seg)))

;(unwind {:a 1 :b 2 :word-seg [{:word 4 :nature 5} {:word 6 :nature 7}]})



(defn word-seg-utility
  [collections target-key]
  (->> collections
       ;(take 5)
       (map #(seg/word-seg target-key %))))

(defn add-word-seg-seq
  [collection]
  (let [func (fn [coll] (->> coll
                             (map :word)
                             (filter #(> (count %) 1))))]
    (map (fn [entry] (update-in entry [:word-seg] func)) collection)))

(defn word-seg-unwind
  [collection]
  (->> collection
       (map #(select-keys % [:word-seg :source :mid :pubdate :_id :keyword :date]))
       (map unwind)
       (apply concat)
       (filter #(> (count (:word %)) 1))
       (map #(syn/han :nature %))
       (map #(assoc (dissoc % :_id) :mid2 (:_id %)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;sentiment analysis;;;;;;;;;;;;;;;;;;;;;;;;;

(defn weibo-facial
  [entry]
  (let [pos-set (->> (mmc/find-maps (mg/get-db "config") "weibo_emoticon" {:emctype 1} {:emcname 1 :_id 0})
                 (map :emcname)
                 set)
        neg-set (->> (mmc/find-maps (mg/get-db "config") "weibo_emoticon" {:emctype 0} {:emcname 1 :_id 0})
                 (map :emcname)
                 set)
        pos (remove nil? (map pos-set (:face entry)))
        neg (remove nil? (map neg-set (:face entry)))]
    (if (nil? (first pos))
      (when-not (nil? (first neg))
        (assoc entry :sentiment "负面" :sent-base (take 5 neg)))
      (if (nil? (first neg))
        (assoc entry :sentiment "正面" :sent-base (take 5 pos))
        (if (> (count neg) (count pos))
          (assoc entry :sentiment "负面" :sent-base neg)
          (assoc entry :sentiment "正面" :sent-base pos))))))



(defn word
  [entry]
  (let [pos-set (->> (mmc/find-maps (mg/get-db "config") "weibo_wordlist" {:wordtype 1} {:wordname 1 :_id 0})
                 (map :wordname)
                 set)
        neg-set (->> (mmc/find-maps (mg/get-db "config") "weibo_emoticon" {:wordtype 0} {:wordname 1 :_id 0})
                 (map :wordname)
                 set)
        pos (remove nil? (map pos-set (:word-seg entry)))
        neg (remove nil? (map neg-set (:word-seg entry)))]
    (if (nil? (first pos))
      (when-not (nil? (first neg))
        (assoc entry :sentiment "负面" :sent-base (take 5 neg)))
      (if (nil? (first neg))
        (assoc entry :sentiment "正面" :sent-base (take 5 pos))
        (if (> (count neg) (count pos))
          (assoc entry :sentiment "负面" :sent-base neg)
          (assoc entry :sentiment "正面" :sent-base pos))))))

(defn sentiment
  [entry]
  (cond (= (:source entry) "weibo")
        (cond (weibo-facial entry) (weibo-facial entry)
              (word entry) (word entry)
              :else (assoc entry :sentiment "uk" :sent-base " "))
        :else
        (if (word entry)
          (word entry)
          (assoc entry :sentiment "uk" :sent-base " "))))

;(sentiment {:face ["[弱]"] :word-seg ["梦想" "时尚" "偶像" "活力"] :source "weibo"})






;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;write result;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn write-result
  [source data-table seg-table]
  (let [col (integrate source)
        seg (word-seg-utility col :text)
        sq (add-word-seg-seq seg)
        emt (map sentiment sq)
        unwind (word-seg-unwind seg)]
    (future (doall (insert-by-part data-table emt)))
    (doall (insert-by-part seg-table unwind))
    ))

(defn filt
  [& fns]
  (apply comp fns))

(defn time-filter
  [start-time end-time entries]
  (let [readin-time #(t/from-time-zone (apply t/date-time %) (t/time-zone-for-offset +8))
        st (readin-time start-time)
        et (readin-time end-time)
        filt #(t/within? (t/interval st et) (:pubdate %))]
    (filter filt entries)))

;(time-filter [2013 10 1] [2014 4 1] (integrate baidu-tianya-source))


(defn black-list
  [file]
  (let [word-list (string/split (slurp file) #"\r\n")]
    (re-pattern (apply str  (first word-list)
                       (map #(str "|" %) (rest word-list))))))


(defn text-filter
  [blacklist column entries]
  (when-let [filt #(not (boolean (re-find (black-list blacklist) (get % column "NULL"))))]
    (filter filt entries)))

(defn synonym-filter
  [syno-map entries]
  (map #(syn/category :keyword % syno-map) entries))



(defn write-result
  ([source filters data-table seg-table]
   (let [col (integrate source)
         filt (filters col)
         seg (word-seg-utility filt :text)
         sq (add-word-seg-seq seg)
         emt (map sentiment sq)
         unwind (word-seg-unwind seg)]
     (future (doall (insert-by-part data-table emt)))
     (doall (insert-by-part seg-table unwind))))
  ([source data-table seg-table]
   (let [col (integrate source)
        seg (word-seg-utility col :text)
        sq (add-word-seg-seq seg)
        emt (map sentiment sq)
        unwind (word-seg-unwind seg)]
    (future (doall (insert-by-part data-table emt)))
    (doall (insert-by-part seg-table unwind))
    )))

#_(def edu-filter (partial synonym-filter syn/edu-synonyms))


;(write-result source "xuetestintegrate" "xuetestsegs")

;贴吧

#_(def tieba-source {:tieba ["car_baidutieba_main" "car_baidutieba_content" :url :url]})

#_(write-result tieba-source "car_tiebaintegrate" "car_tiebasegs")

;百度-天涯

#_(def baidu-tianya-source {:baidu-tianya ["baidurealtime_tianya" "tianya_content" :encrypedLink :url :p5-on]})

;(def filters (filt (partial time-filter [2013 10 1] [2014 4 1])
                   ;(partial text-filter (black-list "D:/data/blacklist.txt") :text)
                   ;))

#_(write-result baidu-tianya-source edu-filter "xuetestintegrate" "xuetestsegs")

;天涯
#_(def tianya-source {:tianya ["car_tianya_search" "car_tianya_content" :url :url]})

#_(write-result tianya-source "car_tianyaintegrate" "car_tianyasegs")

;微博
#_(def weibo-source {:weibo ["car_weibo_history"]})

#_(write-result weibo-source "car_weibointegrate" "car_weibosegs")

;百度新闻

#_(def baidunews-source {:baidu-news ["car_baidunews_history_generic" "car_baidunews_history" :url :url]})

#_(write-result baidunews-source "car_news_integrate" "car_news_segs")

;整合

#_(def integrate-source {:tieba ["car_baidutieba_main" "car_baidutieba_content" :url :url]
                        :tianya ["car_tianya_search" "car_tianya_content" :url :url]
                        :weibo ["car_weibo_history"]
                        })

#_(write-result integrate-source "car_social_integrate" "car_social_segs")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;aggregation;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn word-date-distribution
  ([collection word start-day end-day]
   (let [result (mc/aggregate collection [{$group {:_id {:pubdate "$pubdate" :word "$word" :nature "$nature"} :counts {$sum 1}}}
                              {$match {"_id.pubdate" {$gte (t/from-time-zone (apply t/date-time start-day) (t/time-zone-for-offset +8))
                                                      $lte (t/from-time-zone (apply t/date-time end-day) (t/time-zone-for-offset +8))}
                                       "_id.word" word}}
                              {$sort {"_id.pubdate" 1}}
                              ;{$match {"_id.nature" "形容词"}}
                              ])
        fstr #(l/format-local-time % :date)
        fdate #((comp fstr :pubdate :_id) %)
        fn #(assoc {} :word word :date (fdate %) :counts (:counts %))]
    (map fn result)
    ))
  ([collection start-day end-day]
   (let [result (mc/aggregate collection [{$group {:_id {:pubdate "$pubdate" :word "$word" :nature "$nature"} :counts {$sum 1}}}
                                          {$sort {"_id.pubdate" 1}}
                                          {$match {"_id.pubdate" {$gte (t/from-time-zone (apply t/date-time start-day) (t/time-zone-for-offset +8))
                                                                  $lte (t/from-time-zone (apply t/date-time end-day) (t/time-zone-for-offset +8))}}}
                              ;{$match {"_id.nature" "形容词"}}
                              ])
        fstr #(l/format-local-time % :date)
        fdate #((comp fstr :pubdate :_id) %)
        fword #((comp :word :_id) %)
        fn #(assoc {} :word (fword %) :date (fdate %) :counts (:counts %))]
    (map fn result)
    )))

(defn word-list
  ([collection nature]
  (let [result (mc/aggregate collection [{$group {:_id {:keyword "$keyword":word "$word" :nature "$nature"} :counts {$sum 1}}}
                              {$sort {"counts" -1}}
                              {$match {"_id.nature" nature}}
                              ])
        fword #((comp :word :_id) %)
        fnature #((comp :nature :_id) %)
        fn #(assoc {} :word (fword %) :nature (fnature %) :counts (:counts %))
        ]
    (map fn result)))
  ([collection]
  (let [result (mc/aggregate collection [{$group {:_id {:keyword "$keyword":word "$word" :nature "$nature"} :counts {$sum 1}}}
                              {$sort {"counts" -1}}
                              ;{$match {"_id.nature" nature}}
                              ])
        fkey #((comp :keyword :_id) %)
        fword #((comp :word :_id) %)
        fnature #((comp :nature :_id) %)
        fn #(assoc {} :counts (:counts %) :nature (fnature %) :word (fword %) :keyword (fkey %))
        ]
    (map fn result))))

;(write-excel (word-list "car_weibosegs") "词频" "D:/data/car/微博分词.xlsx")

;(write-excel (word-list "xuetestsegs" "专有名词") "专有名词" "D:/data/专有名词.xlsx")

;(mc/find-maps "xuetestsegs")

;(write-excel (word-date-distribution "xuetestsegs" [1970 1 1] [2014 4 4]) "词频时间分布" "E:/data/词频时间分布.xlsx")

(defn result
  []
  (concat
   (word-date-distribution "xuetestsegs" [1970 1 1] [2009 10 1])
    (word-date-distribution "xuetestsegs" [2009 10 2] [2010 10 1])
    (word-date-distribution "xuetestsegs" [2010 10 2] [2011 10 1])
    (word-date-distribution "xuetestsegs" [2011 10 2] [2012 10 1])
    (word-date-distribution "xuetestsegs" [2012 10 2] [2013 7 1])
    (word-date-distribution "xuetestsegs" [2013 7 2] [2013 8 1])
    (word-date-distribution "xuetestsegs" [2013 8 2] [2013 10 1])
    (word-date-distribution "xuetestsegs" [2013 10 2] [2013 11 1])
           (word-date-distribution "xuetestsegs" [2013 11 2] [2013 12 1])
           (word-date-distribution "xuetestsegs" [2013 12 2] [2014 1 1])
           (word-date-distribution "xuetestsegs" [2014 1 2] [2014 2 1])
           (word-date-distribution "xuetestsegs" [2014 2 2] [2014 3 1])
           (word-date-distribution "xuetestsegs" [2014 3 2] [2014 4 1])))


;(insert-by-part "xuetestworddistribution" (result))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;drilling down;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn drill-down
  [word segs entries start-day end-day]
  (let [text #(:text (mc/find-one-as-map entries {:_id (:mid2 %)} {:_id 0 :text 1}))
        func #(assoc % :text (text %))
        date-range {$gte (t/from-time-zone (apply t/date-time start-day) (t/time-zone-for-offset +8))
                    $lte (t/from-time-zone (apply t/date-time end-day) (t/time-zone-for-offset +8))}
        col (with-collection segs
              (find {:word word :pubdate date-range})
              (sort (array-map :pubdate 1)))]
    (->> (map func col)
         (map #(select-keys % [:source :text :pubdate :word :mid2]))
         distinct
         (map #(assoc % :pubdate (l/format-local-time (:pubdate %) :date)))
         (map #(assoc % :mid2 (str (:mid2 %))))
         )))


;(drill-down "开心" "car_social_segs" "car_social_integrate" [2014 4 8] [2014 5 5])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;adding category;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn category
  [k1 k2 entry synonyms]
  (let [cat (synonyms (get entry k1) (get entry k1))]
    (assoc entry k2 cat)))

(defn synonym
  [excel & sheets]
  (let [data (lazy-workbook (workbook-xssf excel))
        first-sym (->> (mapcat val (select-keys data sheets))
                       (map #(take 2 %))
                       (reduce #(apply assoc %1 %2) {}))
        func #(map second (second %))
        func2 #(map (fn [n] (vector n (first %)))(func %))
        second-sym (->> (mapcat func2 data)
                        (reduce #(apply assoc %1 %2) {}))
        ]
   [first-sym second-sym (map key first-sym)]
  ))

(defn add-category
  [collection excel & sheets]
  (let [func1 #(category :word :topic % (first (apply synonym excel sheets)))
        func2 #(category :topic :category % (second (apply synonym excel sheets)))]
    (->> (map func1 collection)
         (map func2))))

;(add-category [{:word "x"}{:word "e"}] "D:/data/heihei.xlsx" "haha")

;(first(rest (synonym "D:/data/星星分词.xlsx" "人物" "名词" "形容词")))

;(synonym "D:/data/car/车展分词更新版.xlsx" "汽车专有名词" "名词" "形容词" "车展人物")

(defn categorize-data
  [segs entries start-day end-day excel & sheets]
  (apply (partial add-category
                  (mapcat #(drill-down % segs entries start-day end-day)
                           (second (rest (apply synonym excel sheets))))
                  excel)
         sheets))

#_(-> (categorize-data "car_social_segs" "car_social_integrate" [2014 4 8] [2014 5 5]
                 "D:/data/car/车展分词更新版.xlsx" "汽车专有名词" "名词" "形容词" "车展人物")
    (write-excel "话题分类" "D:/data/car/话题分类.xlsx"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;excel output;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn write-excel
  [collection sheet file]
  (let [func #(map val %)
        cols (map key (first collection))]
    (->> collection
         (map #(func %))
         (#(build-workbook (workbook-xssf) {sheet (into (vector cols) %)}))
         (#(save % file))
         )))

;(write-excel (drill-down "都敏俊" "xuetestsegs" "xuetestentries" [2013 12 1] [2014 3 1]) "haha" "D:/data/教授.xlsx")


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;user analysis;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-keys
  [coll]
  (reduce #(apply conj %1 (keys %2)) #{} coll))


;(def abcd (mc/find-maps "car_weibo_history_detaileduser"))

;(doall (get-keys abcd))

(defn accumulator
  [the-key coll]
  (let [func (fn [x] (->> x frequencies (clojure.core/sort #(> (val %1) (val %2)))))
        coll2 (->> coll (map #(dissoc % :_systime :_id)) distinct)]
    (loop [c coll]
      (when-not (empty? c)
        (if-not (nil? (get (first c) the-key))
          (if (vector? (get (first c) the-key))
            (func (mapcat the-key coll2))
            (func (map the-key coll2)))
          (recur (rest c)))))))



;(accumulator :信用等级 abcd)

(defn age-estimated-utility
  [entry]
  (let [regex #(or (re-find #"\(([^\(年\)]+)年\)" %) (re-find #"(\d+)年\d+月\d+日" %))
        since #(->> (regex %)
                    second
                    read-string
                    (- (t/year (t/today))))
        edu [:高中 :中专技校 :初中 :小学 :大学 :生日]
        edu-age {:生日 0 :高中 15 :中专技校 15 :初中 12 :小学 6 :大学 18}
        birth (get entry :生日)]
    (loop [a edu]
      (let [item (get entry (first a))]
        (when-not (empty? a)
          (if (and (not (nil? item)) (not (nil? (regex item))))
            (let [age (+ (get edu-age (first a)) (since item))]
              (if (and (>= age 5) (< age 100))
                age
                (recur (rest a))))
            (recur (rest a))))))))

(defn edu-estimated
  [entry]
  (let [edu [:大学 :高中 :中专技校 :初中 :小学]]
    (loop [e edu]
      (let [item (get entry (first e))]
        (when-not (empty? e)
          (if-not (nil? item)
            (name (first e))
            (recur (rest e))))))))

(defn active-level
  [entry]
  (when (and (not (nil? (:注册时间 entry))) (not (nil? (:活跃天数 entry))))
    (let [reg (:注册时间 entry)
          active (:活跃天数 entry)
          life (t/in-days (t/interval (parse-date reg) (t/now)))]
      (double (/ active life)))))

(defn extract-user
  [entry]
  {:fans_numbers (:粉丝 entry)
   :active_score (active-level entry)
   :credit (:信用等级 entry)
   :tags (string/join " " (:标签 entry))
   :city (last (:所在地 entry))
   :province (first (:所在地 entry))
   :edu_level_estimated (edu-estimated entry)
   :age_estimated (age-estimated-utility entry)
   :gender (:性别 entry)
   :userName (:昵称 entry)
   :userId (:userId entry)})

#_(-> (map extract-user abcd)
    (write-excel "微博用户信息统计表" "D:/data/car/微博用户信息统计表.xlsx"))




;(remove nil? (map edu-estimated megauser))

;(frequencies (remove nil? (map age-estimated-utility abcd)))

#_(def megauser (mg/with-connection
               (mg/connect! {:host "192.168.1.184" :port 27017})
               (mg/with-db (mg/get-db "megausers")
                           (mc/find-maps "users"))))

(def connection-2
  (mg/connect {:host "192.168.1.184" :port 27017}))

(def db-2 (mg/get-db connection-2 "megausers"))

(def megauser (mmc/find-maps db-2 "users"))

#_(-> (map extract-user megauser)
    (write-excel "微博用户信息统计表" "D:/data/car/微博用户信息统计表.xlsx"))


;(remove nil? (map age-estimated-utility megauser))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;working zone;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(mg/connect! {:host "192.168.1.184" :port 7017})

(mg/set-db! (mg/get-db "lightdata"))


(def locations {:tianya "star_tianya_content"
             :douban "star_douban_shortcomments"
             :tieba "star_baidutieba_contents"
             :gada "haha"
             :weibo "star_weibo_history"
             ;:youku "star_youku_video"
               })

(def locations2 {:tianya "xuetesttianya"
             :douban "xuetestdouban"
             :tieba "xuetesttieba"
             :gada "haha"
             :weibo "xuetestweibo"
             ;:youku "star_youku_video"
               })

;去噪

#_(->> (mc/find-maps "star_weibo_history")
     (map #(dissoc % :_id :systime))
     distinct
     (insert-by-part "xuetestweibo"))

#_(->> (mc/find-maps "star_tianya_content")
     (map #(dissoc % :_id))
     distinct
     (insert-by-part "xuetesttianya"))

#_(->> (mc/find-maps "star_baidutieba_contents")
     (map #(dissoc % :_id :systime))
     distinct
     (insert-by-part "xuetesttieba"))

#_(->> (mc/find-maps "star_douban_shortcomments")
     (map #(dissoc % :_id))
     distinct
     (insert-by-part "xuetestdouban"))



;提取主变量

#_(insert-by-part "xuetest" (integrate-text locations2))

#_(insert-by-part "xuetestentries" (all-entries-joda locations2))


;分词

#_(insert-by-part "xuetestsegs" (word-seg-all (mc/find-maps "xuetestentries")))

;提取正文和概念整合
#_(insert-by-part "xuetestmaterial" (add-category (mapcat #(drill-down % "xuetestsegs" "xuetestentries" [2013 12 25] [2014 3 1])
                                                        (second (rest (synonym "D:/data/星星分词0402.xlsx" "人物" "概念" "描述"))))
              "D:/data/星星分词0402.xlsx"
              "人物" "概念" "描述"))

#_(->> (mc/find-maps "car_news_integrate" {} {:_id 0 :pubdate 1 :title 1 :sentiment 1 :sent-base 1 :similar 1 :keyword 1 :preview 1
                                        :source 1 :origin 1})
     (map #(select-keys % [:pubdate :sent-base :sentiment :preview :title :similar :source :keyword :origin]))
     (map #(assoc % :pubdate (unparse-date (:pubdate %))))
     (map #(assoc % :sent-base (str (string/join " " (:sent-base %)) " ")))
     (#(write-excel % "新闻" "D:/data/car/新闻列表.xlsx"))
     )



#_(->> (mc/find-maps "car_social_integrate" {} {:_id 0 :mid 0 :word-seg 0})
   (map #(select-keys % [:pubdate :url :user :level :sent-base :sentiment :text :title :source :keyword]))
     (map #(assoc % :pubdate (unparse-date (:pubdate %))))
     (map #(assoc % :sent-base (str (string/join " " (:sent-base %)) " ")))
     (#(write-excel % "social" "D:/data/car/social列表.xlsx")))

;;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;;;;

(flatten [[{:a 2} {:b 3}] [{:c 4} {:d 5}]])


#_(f/show-formatters)

(re-find #"\(([^\(年\)]+)年\)" "(1985年)春天")

(re-find #"(\d+)年\d+月\d+日" "1985年10月5日")

(re-find #"(\d+)\-(\d+)\-(\d+)" "2012-3-4")

(t/in-days (t/interval (t/date-time 1986 10 2) (t/date-time 1986 10 14)))
