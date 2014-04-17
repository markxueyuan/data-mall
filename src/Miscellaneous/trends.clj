(ns Miscellaneous.trends
  (:refer-clojure :exclude [find])
  (:require [clojure.string :as string]
            [data-mall.connectDB3 :as db]
            [clojure.java.jdbc :as jdbc]
            [monger.core :as mg];the following 4 is for mongo use
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.joda-time :as mjt]
            [monger.multi.collection :as mmc]
            [data-mall.ansj-seg :as seg]
            [data-mall.synonym :as syn]
            [data-mall.pivot-table :as pt]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as joda]
            [clj-time.local :as l]
            [data-mall.moving-average :as mv]
            )
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern];the following two is for mongo use
           org.bson.types.ObjectId)
  (:use clj-excel.core))

(mg/connect! {:host "192.168.1.184" :port 7017})

(mg/set-db! (mg/get-db "xuetest"))


(mc/find-maps "mahang_baidunews_counts")

(defn read-time-num
  [time-num]
  (->> (* time-num 1000)
       (joda/from-long)
       (#(t/to-time-zone % (t/time-zone-for-offset +8)))
       (f/unparse (f/formatter (t/default-time-zone) "YYYY-MM-dd" "YYYY/MM/dd"))))

(defn read-baidu-trends
  [coll]
  (let [correct-nil #(if (nil? %) 0 %)]
    (->> (map #(update-in % [:start] read-time-num) coll)
         ;(map #(update-in % [:count] correct-nil))
         (map #(select-keys % [:count :start :keyword]))
         (map #(assoc % :date (:start %)))
         (map #(dissoc % :start)))))

(defn read-weibo-daily-trends
  [coll]
  (let [year #(:beginyear (:input %))
        month #(:beginmonth (:input %))
        day #(:begindate (:input %))
        date #(str (year %) "-" (month %) "-" (day %))
        keyword #(:keyword (:input %))]
    (map #(assoc {} :count (:count %) :date (date %) :keyword (keyword %)) coll)))

#_(->> "mahang_baidunews_counts"
     mc/find-maps
     read-baidu-trends
     (#(write-excel % "百度新闻趋势" "D:/data/mahang/百度新闻趋势.xlsx")))

#_(->> "mahang_history"
     mc/find-maps
     read-weibo-daily-trends
     (#(write-excel % "微博趋势" "D:/data/mahang/微博趋势.xlsx")))

(defn read-weibo-hourly-trends
  [coll]
  (let [correct-nil #(if (nil? %) 0 %)
        year #(:beginyear (:input %))
        month #(:beginmonth (:input %))
        day #(:begindate (:input %))
        date #(t/from-time-zone (t/date-time (year %) (month %) (day %)) (t/time-zone-for-offset +8))
        hour #(:beginhour (:input %))
        ct #(correct-nil (:count %))]
    (map #(assoc {} :date (date %) :hour (hour %) :count (ct %)) coll)))


(read-weibo-hourly-trends (mc/find-maps "mahangdailycounts"))

(defn sum-date
  [coll]
  (->> (pt/pivot-table [:date] [:count] [pt/sum] coll)
       (reduce #(assoc %1 (:date %2) (:count %2)) {})))

(sum-date (read-weibo-hourly-trends (mc/find-maps "mahangdailycounts")))

(defn hourly-share
  [coll]
  (let [data (read-weibo-hourly-trends coll)
        sum-date (sum-date data)
        get-av #(assoc % :average (/ (:count %) (get sum-date (:date %))))]
    (map get-av data)))

(hourly-share (mc/find-maps "mahangdailycounts"))


(defn moving-average
  [coll hour window]
  (let [type-time #(f/unparse (f/formatter (t/default-time-zone) "YYYY-MM-dd" "YYYY/MM/dd") %)
        data (->> coll
                  hourly-share
                  (filter #(= hour (:hour %)))
                  (sort #(compare (:date %1) (:date %2))))
        counts (map :average data)
        mv-av (mv/moving-average window counts)
        dates (map (comp type-time :date) data)]
    (map #(assoc {} %1 %2) dates mv-av)))

(moving-average (mc/find-maps "mahangdailycounts") 12 7)


(defn moving-average-matrix
  [collection window]
  (let [material (for [x (range 24)]
                   (let [mv-av (moving-average collection x window)
                         col (map (comp key first) mv-av)
                         value (map (comp val first) mv-av)]
                     [col value]))]
    (->> (concat (vector (ffirst material)) (map second material))
         (apply map vector)
         (concat (vector (reduce conj [nil] (range 24))))
         )))

(moving-average-matrix (mc/find-maps "mahangdailycounts") 7)

(-> (build-workbook (workbook-xssf) {"移动平均" (moving-average-matrix (mc/find-maps "mahangdailycounts") 41)})
    (save "D:/data/移动平均.xlsx"))

