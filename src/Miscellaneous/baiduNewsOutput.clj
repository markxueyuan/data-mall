(ns Miscellaneous.baiduNewsOutput
  (:require [clojure.java.jdbc :as db]
           [clojure.string :as s]
           [data-mall.toCSVorJSON :as toCSV]
           [monger.core :as mg]
           [monger.collection :as mc]
           [monger.operators :refer :all])
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern]
           org.bson.types.ObjectId))

(def db-spec
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//192.168.3.52:3306/credit_card"
   :user "root"
   :password "eura_ds"})

(def input (db/query db-spec ["select * from credit_card_baidunews_0312_date_time_kw_total"]))

(defn text-filter
  [string]
  (let [strings (s/split string #"[,\t\r\n　]+")]
    (apply str(interpose "，" strings))))

(defn tuple-filter
  [tuples & keywords]
  (let [h #(select-keys % keywords)
        f #(assoc %1 %2 (text-filter (%2 %1)))
        g #(reduce f % (keys %))
        j #(into %1 %2)]
    (map j tuples (map g (map h tuples)))))

;(tuple-filter input :title :preview)


(defn change-keywords
  [tuples & {:as match}]
  (let [f #(keyword (second %))
        g #(vector (f %2) ((first %2) %1))
        h #(map (partial g %) match)
        j #(apply (partial dissoc %) (keys match))
        k #(into (j %) (h %))
        ]
    (map k tuples)))

;(change-keywords [{:a 2 :b 3} {:a 4 :b 5}] :a "d" :b "e")



;(clojure.set/rename-keys {:a 2 :b 3} {:a "d" :b "e"})

;(change-keywords (tuple-filter input :title :preview) :title "标题" :origin "来源" :keywords "关键词")

;(toCSV/toCSV2 [:标题 :来源 :关键词] "D:/data/filtered-data" (change-keywords (tuple-filter input :title :preview) :title "标题" :origin "来源" :keywords "关键词"))





