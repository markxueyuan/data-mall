(ns Miscellaneous.unicom
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
            [data-mall.pivot-table :as pt])
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern];the following two is for mongo use
           org.bson.types.ObjectId))


(mg/connect! {:host "192.168.3.53" :port 7017})

(mg/set-db! (mg/get-db "unicom"))

(defn all [] (mc/find-maps "weibo_history_count"
              {(keyword "opts.removeDups") ""}
              {(keyword "input.keyword") 1
               (keyword "input.beginyear") 1
               (keyword "input.beginmonth") 1
               (keyword "input.begindate") 1
               :count 1
               :_id 0}))

(defn extract [] (mc/find-maps "weibo_history_count"
              {(keyword "opts.removeDups") "true"}
              {(keyword "input.keyword") 1
               (keyword "input.beginyear") 1
               (keyword "input.beginmonth") 1
               (keyword "input.begindate") 1
               :count 1
               :_id 0}))


(defn set
  [dta-set count-key]
  (->> (dta-set)
       (map #(assoc (:input %) count-key (:count %)))
       (map #(assoc % :date (str (:beginyear %) "-" (:beginmonth %) "-" (:begindate %))))
       (map #(dissoc % :beginyear :beginmonth :begindate))
       (sort-by (juxt :keyword :date))))

(set extract :extract-count)



(defn cp2
  [set1 set2 key1 key2 k-1 k-2]
  (let [c (fn [key s1 s2] (compare (key (first s1)) (key (first s2))))]
    (loop [s1 set1 s2 set2 col []]
      (let [e1 (first s1) e2 (first s2)]
            (cond (and (nil? e1) (not (nil? e2))) (reduce #(merge %1 %2) col (map #(assoc % k-1 0) s2))
                  (and (not (nil? e1)) (nil? e2)) (reduce #(merge %1 %2) col (map #(assoc % k-2 0) s1))
                  (and (nil? e1) (nil? e2)) col
                  (> (c key1 s1 s2) 0) (recur s1 (rest s2) (conj col (assoc e2 k-1 0)))
                  (< (c key1 s1 s2) 0) (recur (rest s1) s2 (conj col (assoc e1 k-2 0)))
                  (> (c key2 s1 s2) 0) (recur s1 (rest s2) (conj col (assoc e2 k-1 0)))
                  (< (c key2 s1 s2) 0) (recur (rest s1) s2 (conj col (assoc e1 k-2 0)))
                  (= (c key2 s1 s2) 0) (recur (rest s1) (rest s2) (conj col (into e1 e2))))))))





;(cp2 (set all :all-count) (set extract :extract-count) :keyword :date :all-count :extract-count)


