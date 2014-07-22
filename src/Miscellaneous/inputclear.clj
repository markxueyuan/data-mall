(ns Miscellaneous.inputclear
  (:require [net.cgrand.enlive-html :as h]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.multi.collection :as mmc];mirror of mc, but with db as first argument for every function.
            [monger.conversion :refer [from-db-object]]
            ;[monger.query :refer ($lte)]
            [clojure.string :as string]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as joda]
            [clj-time.local :as l])
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern BasicDBObject BasicDBList]
           org.bson.types.ObjectId
           java.util.ArrayList))

(def connection (mg/connect {:host "192.168.1.184" :port 7017}))

(def db (mg/get-db connection "lightdata"))

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


#_(defn interval-predict
  [start-day end-day date]
  (let [f1 #(t/from-time-zone % (t/time-zone-for-offset +8))
        f2 #(apply t/date-time %)
        start (f1 (f2 start-day))
        end (f1 (f2 end-day))
        date (f1 (parse-date date))]
    (and (<= (compare start date) 0)
         (<= (compare date end) 0))))

(defn time-filter
  [start-time end-time col entries]
  (let [readin-time #(t/from-time-zone (apply t/date-time %) (t/time-zone-for-offset +8))
        st (readin-time start-time)
        et (readin-time end-time)
        filt #(t/within? (t/interval st et) (if (try (parse-date (col %))
                                                  (catch Throwable e))
                                              (parse-date (col %))
                                              (t/to-time-zone (t/now) (t/time-zone-for-offset +8))))]
    (filter filt entries)))

(defn p5-filter
  [entries]
  (let [a (atom #{})
        b (atom [])]
    (doseq [q entries]
      (let [p5 ((juxt :p5 :keywords) q)]
        (when-not (get @a p5)
          (swap! a #(conj % p5))
          (swap! b #(conj % q)))))
    @b))

(defn insert-clean-url
  [db collection col coll]
  (let [db (mg/get-db connection db)]
    (->> coll
         (map col)
         distinct
         (map #(assoc {} :_id %))
         (mmc/insert-batch db collection))))

#_(->> (mmc/find-maps db "biaoge_baidu_for_tianya" {:p5 {"$lte" 50}})
     (time-filter [2014 7 6] [2014 7 16] :date)
     p5-filter
     (insert-clean-url "gu_chain" "biaoge_tianya" :encrypedLink))




