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
            [data-mall.ansj-seg :as seg]
            [data-mall.synonym :as syn]
            [data-mall.pivot-table :as pt]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as joda]
            [clj-time.local :as l]
            [clojure.string :as string]
            )
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern];the following two is for mongo use
           org.bson.types.ObjectId)
  (:use clj-excel.core))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;extract text ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



(defn insert-by-part
  [collection data]
  (let [parts (partition-all 500 data)]
    (map #(mc/insert-batch collection %) parts)))



(mg/connect! {:host "192.168.1.184" :port 7017})

;(mg/connect!)
(mg/set-db! (mg/get-db "xuetest"))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;link main;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-key
  [from-col to-col from-key to-key]
  (mc/ensure-index from-col {from-key 1})
  (let [col (mc/find-maps to-col)]
    (for [entry col]
      (let [match (mc/find-one-as-map from-col {from-key (get entry to-key)})]
        (assoc entry :keyword (:keywords match))))))

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

;(insert-by-part "xuetesttianyaextract" (map #(extract-tianya %) (mc/find-maps "xuetesttianya")))

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
        url (:weiboUrl entry)]
    {:user user :pubdate date :level level :text text :_id (ObjectId.) :mid mid :keyword kw :source source :origPostUrl original :userId userid :url url}))



;(insert-by-part "xuetesweiboextract" (map #(extract-weibo %) (mc/find-maps "weibo_history")))


(defn extract-douban
  [entry])

(defn extract-youku
  [entry])



(defn prepare-data
  ([extract-fn from-col to-col from-key to-key]
   (->> (get-key from-col to-col from-key to-key)
        (map extract-fn)))
  ([extract-fn col]
   (map extract-fn (read-data col))))

;(prepare-data extract-weibo "weibo_history")

;(prepare-data extract-tieba "baidu_tieba_main" "baidu_tieba_contents" :url :url)


(defn utility
  [source-key source-docs]
  (cond (= source-key :tianya) (apply (partial prepare-data extract-tianya) source-docs)
        (= source-key :tieba) (apply concat (apply (partial prepare-data extract-tieba) source-docs))
        (= source-key :weibo) (apply (partial prepare-data extract-weibo) source-docs)
        (= source-key :douban) (apply (partial prepare-data extract-douban) source-docs)
        (= source-key :youku) (apply (partial prepare-data extract-youku) source-docs)))


(defn integrate
  [{:as source}]
  (let [m [:tianya :tieba :weibo :douban :youku]
        s (set (keys source))
        job (filter s m)]
    (mapcat #(utility % (get source %)) job)))

(def source {:weibo ["weibo_history"]
             :tianya ["tianya_search" "tianya_content" :url :url]
             :tieba ["baidu_tieba_main" "baidu_tieba_contents" :url :url]
             :haha []})

;(insert-by-part "xuetestintegrate" (integrate source))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;word-seg;;;;;;;;;;;;;;;;;;;;;;;

(defn unwind
  [entry]
  (let [pivot (dissoc entry :word-seg)
        word-seg (get entry :word-seg)
        func #(into pivot %)]
    (map func word-seg)))

;(unwind {:a 1 :b 2 :word-seg [{:word 4 :nature 5} {:word 6 :nature 7}]})

(defn word-seg-utility
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

;(insert-by-part "word_count" (word-seg-utility (mc/find-maps "xuetest") :text :source :mid))



(defn word-seg
  [collection]
  (->> collection
       (#(word-seg-utility % :text :source :mid :pubdate :_id))
       (map #(assoc (dissoc % :_id) :mid2 (:_id %)))))

;(insert-by-part "xuetestsegs"(word-seg (mc/find-maps "xuetestentries")))


(defn write-result
  [source data-table seg-table]
  (let [col (integrate source)
        seg (word-seg col)]
    (future (doall (insert-by-part data-table col)))
    (doall (insert-by-part seg-table seg))
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

(time-filter [2013 10 1] [2014 4 1] (integrate baidu-tianya-source))


(defn black-list
  [file]
  (let [word-list (string/split (slurp file) #"\r\n")]
    (re-pattern (apply str  (first word-list)
                       (map #(str "|" %) (rest word-list))))))


(defn text-filter
  [blacklist column entries]
  (when-let [filt #(not (boolean (re-find (black-list blacklist) (get % column "NULL"))))]
    (filter filt entries)))



(defn write-result
  ([source filters data-table seg-table]
  (let [col (integrate source)
        filt (filters col)
        seg (word-seg filt)]
    (future (doall (insert-by-part data-table filt)))
    (doall (insert-by-part seg-table seg))
    ))
  ([source data-table seg-table]
   (let [col (integrate source)
        seg (word-seg col)]
    (future (doall (insert-by-part data-table col)))
    (doall (insert-by-part seg-table seg))
    )))


;(write-result source "xuetestintegrate" "xuetestsegs")

;(def tieba-source {:tieba ["baidu_tieba_main" "baidu_tieba_contents" :url :url]})

;(write-result tieba-source "xuetestintegrate" "xuetestsegs")

(def baidu-tianya-source {:tianya ["baidurealtime_tianya0404" "tianya_content0404" :encrypedLink :url]})

(def filters (filt (partial time-filter [2013 10 1] [2014 4 1])
                   ;(partial text-filter (black-list "D:/data/blacklist.txt") :text)
                   ))

(write-result baidu-tianya-source filters "xuetestintegrate2" "xuetestsegs2")

;(write-result baidu-tianya-source "xuetestintegrate" "xuetestsegs")



;(def weibo-source {:weibo ["weibo_history"]})

;(write-result weibo-source "xuetestintegrate" "xuetestsegs")

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
  [collection nature]
  (let [result (mc/aggregate collection [{$group {:_id {:word "$word" :nature "$nature"} :counts {$sum 1}}}
                              {$sort {"counts" -1}}
                              {$match {"_id.nature" nature}}
                              ])
        fword #((comp :word :_id) %)
        fnature #((comp :nature :_id) %)
        fn #(assoc {} :word (fword %) :nature (fnature %) :counts (:counts %))
        ]
    (map fn result)))

;(write-excel (word-list "xuetestsegs" "专有名词") "专有名词" "D:/data/专有名词.xlsx")

;(mc/find-maps "xuetestsegs")



(defmacro result [] `(concat (word-date-distribution "xuetestsegs" [2013 10 1] [2013 11 1])
(word-date-distribution "xuetestsegs" [2013 11 2] [2013 12 1])
(word-date-distribution "xuetestsegs" [2013 12 2] [2014 1 1])
(word-date-distribution "xuetestsegs" [2014 1 2] [2014 2 1])
(word-date-distribution "xuetestsegs" [2014 2 2] [2014 3 1])
(word-date-distribution "xuetestsegs" [2014 3 2] [2014 4 1])))

;(result)

;(write-excel (pt/pivot-table [:date :word] [:counts] [pt/sum] (result)) "词频" "E:/data/教育词频.xlsx")





;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;drilling down;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn drill-down
  [word segs entries start-day end-day]
  (let [text #(:text (mc/find-one-as-map entries {:_id (:mid2 %)} {:_id 0 :text 1}))
        func #(assoc % :text (text %))
        date-range {$gte (t/from-time-zone (apply t/date-time start-day) (t/time-zone-for-offset +8))
                    $lte (t/from-time-zone (apply t/date-time end-day) (t/time-zone-for-offset +8))}
        col (with-collection "xuetestsegs"
              (find {:word word :pubdate date-range})
              (sort (array-map :pubdate 1)))]
    (->> (map func col)
         (map #(select-keys % [:source :text :pubdate :word :mid2]))
         distinct
         (map #(assoc % :pubdate (l/format-local-time (:pubdate %) :date)))
         (map #(assoc % :mid2 (str (:mid2 %))))
         )))

;(drill-down "开心" "xuetestsegs" "xuetestentries" [2013 12 1] [2014 3 1])


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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;working zone;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;;;;;;;;;;;;;;;;;;;;;;来自星星的你;;;;;;;;;;;;;;;;;

;连接数据库
#_(mg/connect! {:host "192.168.3.53" :port 7017})

#_(mg/connect!)

#_(mg/set-db! (mg/get-db "test"))

#_(mg/set-db! (mg/get-db "star"))

(def locations {:tianya "star_tianya_content"
             :douban "star_douban_shortcomments"
             :tieba "star_baidutieba_contents"
             :gada "haha"
             :weibo "star_weibo_history"
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

(def locations2 {:tianya "xuetesttianya"
             :douban "xuetestdouban"
             :tieba "xuetesttieba"
             :gada "haha"
             :weibo "xuetestweibo"
             ;:youku "star_youku_video"
               })

;提取主变量

#_(insert-by-part "xuetest" (integrate-text locations2))

#_(insert-by-part "xuetestentries" (all-entries-joda locations2))


;分词

#_(insert-by-part "xuetestsegs" (word-seg-all (mc/find-maps "xuetestentries")))

;提取正文和概念整合
(insert-by-part "xuetestmaterial" (add-category (mapcat #(drill-down % "xuetestsegs" "xuetestentries" [2013 12 25] [2014 3 1])
                                                        (second (rest (synonym "D:/data/星星分词0402.xlsx" "人物" "概念" "描述"))))
              "D:/data/星星分词0402.xlsx"
              "人物" "概念" "描述"))

;汇总、导出excel

#_(-> (word-date-distribution "xuetestsegs" [2013 12 25] [2014 3 1])
    (write-excel "词频时间分布" "D:/data/来自星星的你social词频.xlsx"))

#_(->> (mc/find-maps "xuetestmaterial")
     (map #(dissoc % :_id))
     (#(write-excel % "social数据分类" "D:/data/来自星星的你social数据.xlsx"))
     )

(def black-list [])

(def white-list [])

;;;;;;;;;;;;;;;;;;;;;;;;;教育品牌;;;;;;;;;;;;;;;;;;;;;;;

;连接数据库
;(mg/connect! {:host "192.168.3.53" :port 7017})

;(mg/set-db! (mg/get-db "edu"))



#_(def locations {:tianya "tianya_content"
                :tieba "baidu_tieba_contents"
                :gada "haha"
                :weibo "star_weibo_history"
                })


;(insert-by-part "xuetest" (integrate-text locations))

;(insert-by-part "xuetestentries" (all-entries-joda locations))
;;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;;;;

(flatten [[{:a 2} {:b 3}] [{:c 4} {:d 5}]])

#_(f/show-formatters)


