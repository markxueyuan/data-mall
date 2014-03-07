(ns Miscellaneous.taobao
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as joda]
            [data-mall.pivot-table :as p]
            [data-mall.toCSVorJSON :as toCSV])
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern]
           org.bson.types.ObjectId))

(mg/connect! {:host "192.168.3.53" :port 7017})

(mg/set-db! (mg/get-db "huawei"))

(def formatter (f/formatters :date-time))

(defn correct-nil
  [stuff col-key entry]
  (if (or (= "" (col-key entry))(nil? (col-key entry)))
    (assoc entry col-key stuff)
  entry))

(def start-time
  (->> (t/date-time 2014 3 7 10 0 0)
     (#(t/from-time-zone % (t/time-zone-for-offset +8)))
     (joda/to-long)
     ))

(->> (mc/find-maps "taobaoComments")
     (map :_systime)
     (map #(f/parse formatter %))
     (map #(t/to-time-zone % (t/time-zone-for-offset +8)))
    ; (map #(joda/to-long %))
     ;(map #(- % start-time))
     ;(map #(/ % 60000))
     (map t/hour)
     frequencies
     )

(f/parse formatter "2014-03-07T02:12:24.789Z")
(t/month (t/from-time-zone (f/parse formatter "2014-03-07T02:12:24.789Z") (t/time-zone-for-offset +8)))
(f/parse formatter "2014-03-07T02:12:24.789Z")



(->> (mc/find-maps "taobaoImpression")
     ;identity
     ;(map :text)
     ;(map #({:text %}))
     ;frequencies
     (map #(assoc {} :text (:text %) :count (:count %)))
     (p/pivot-table [:text] [:count] [p/sum])
     ;frequencies
     ;(map #(assoc (first %) :counts (second %)))
     ;(remove nil?)
     (sort #(> (:count %1) (:count %2)))
     (toCSV/toCSV2 [:text :count] "D:/data/淘宝.csv")
     )

(->> (mc/find-maps "taobaoSearch")
     (map #(:如实描述 (:scores %)))
     (sort <)
     (remove #(= % 0))
     p/average
     )

(->> (mc/find-maps "taobaoSearch")
     (map #(:price %))
     (sort <)
     (remove #(= % 0))
     p/average
     )

(->> (mc/find-maps "taobaoSearch")
     (map #(assoc {} :sold30days (:sold30days %) :score (:如实描述 (:scores %))))
     (remove #(or (= (:sold30days %) 0) (= (:score %) 0)))
     (toCSV/toCSV2 [:sold30days :score] "D:/data/淘宝.csv")
     )

(->> (mc/find-maps "taobao"))




f/formatters

