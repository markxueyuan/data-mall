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
            [monger.joda-time :as mjt]
            [data-mall.ansj-seg :as seg]
            [data-mall.synonym :as syn]
            [data-mall.pivot-table :as pt]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as joda]
            [clj-time.local :as l]
            )
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern];the following two is for mongo use
           org.bson.types.ObjectId)
  (:use clj-excel.core))


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

;(integrate-text locations)

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
  (assoc entry :pubdate (l/format-local-time (extract-date location entry) :date)))

(defn associate-joda-date
  [location {:as entry}]
  (let [joda (extract-date location entry)
        string (l/format-local-time (extract-date location entry) :date)
        date (f/parse (f/formatter (t/default-time-zone) "YYYY-MM-dd" "YYYY/MM/dd") string)]
    (assoc entry :pubdate date)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;all entries;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn all-entries
  [locations]
  (->> locations
       integrate-text
       (map #(associate-date locations %))
       ))

(defn all-entries-joda
  [locations]
  (->> locations
       integrate-text
       (map #(associate-joda-date locations %))
       ))

;(all-entries locations)


;(insert-by-part "xuetestentries" (all-entries-joda locations))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;word-seg-all;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-all
  [locations]
  (->> locations
       integrate-text
       (map #(associate-date locations %))
       (#(word-seg % :text :source :mid :pubdate :_id))))

(defn make-all-joda
  [locations]
  (->> locations
       integrate-text
       (map #(associate-joda-date locations %))
       (#(word-seg % :text :source :mid :pubdate :_id))
       ;(take 5)
       ))

;(make-all-joda locations)

(defn word-seg-all
  [collection]
  (->> collection
       (#(word-seg % :text :source :mid :pubdate :_id))
       (map #(assoc (dissoc % :_id) :mid2 (:_id %)))))

;(insert-by-part "xuetestsegs"(word-seg-all (mc/find-maps "xuetestentries")))

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

(drill-down "开心" "xuetestsegs" "xuetestentries" [2013 12 1] [2014 3 1])


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


;(associate-joda-date locations {:source "weibo" :mid (ObjectId. "530ec0ad07b83b420009e8b5")})

#_(->> (make-all locations)
     (insert-by-part "xuetestall"))


#_(->> (make-all-joda locations)
     (insert-by-part "xuetestall"))





;(word-date-distribution "xuetestall" [2014 2 1] [2014 3 1])

(def black-list [])

(def white-list [])




#_(word-list "xuetestall" "人名")



#_(write-excel (word-list "xuetestall" "人名") "haha" "D:/data/人名.xlsx")

#_(write-excel (add-category (mapcat #(drill-down % "xuetestsegs" "xuetestentries" [2013 12 1] [2014 3 1]) (second (rest (synonym "D:/data/星星分词.xlsx" "人物" "名词" "形容词"))))
              "D:/data/星星分词.xlsx"
              "人物" "名词" "形容词")
             "数据"
             "D:/data/测它一下")

#_(count (add-category (mapcat #(drill-down % "xuetestsegs" "xuetestentries" [2013 12 1] [2014 3 1]) (second (rest (synonym "D:/data/星星分词.xlsx" "人物" "名词" "形容词"))))
              "D:/data/星星分词.xlsx"
              "人物" "名词" "形容词"))

(insert-by-part "xuetestmaterial" (add-category (mapcat #(drill-down % "xuetestsegs" "xuetestentries" [2013 12 1] [2014 3 1])
                                                        (second (rest (synonym "D:/data/星星分词2.xlsx" "人物" "概念" "描述" "元素"))))
              "D:/data/星星分词2.xlsx"
              "人物" "概念" "描述" "元素"))

(add-category (mapcat #(drill-down % "xuetestsegs" "xuetestentries" [2013 12 1] [2014 3 1])
                                                        (second (rest (synonym "D:/data/星星分词2.xlsx" "人物" "概念" "描述" "元素"))))
              "D:/data/星星分词2.xlsx"
              "人物" "概念" "描述" "元素")

(second (rest (synonym "D:/data/星星分词2.xlsx" "人物" "概念" "描述" "元素")))

(drill-down "开心" "xuetestsegs" "xuetestentries" [2013 12 1] [2014 3 1])

(write-excel (map #(dissoc % :_id) (mc/find-maps "xuetestmaterial")) "data" "D:/data/呵呵.xlsx")

;;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;;;;

(flatten [[{:a 2} {:b 3}] [{:c 4} {:d 5}]])

#_(f/show-formatters)

(drill-down % "xuetestsegs" "xuetestentries" [2013 12 1] [2014 3 1])
