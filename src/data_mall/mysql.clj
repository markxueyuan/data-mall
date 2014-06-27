(ns data-mall.mysql
  (:require [clojure.java.jdbc :as j]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.result :as mr]
            [monger.multi.collection :as mmc];mirror of mc, but with db as first argument for every function.
            [monger.conversion :refer [from-db-object]]
            ;[monger.query :refer :all :as mq]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as joda]
            [clj-time.local :as l])
    (:import [com.mongodb MongoOptions ServerAddress WriteConcern BasicDBObject BasicDBList]
           org.bson.types.ObjectId
           java.util.ArrayList))


 (def sql-spec
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname (str "//localhost:3306/purui")
   :user "root"
   :password "othniel"})


(def mongo-spec
  (let [connection (mg/connect {:host "192.168.1.184" :port 7017})]
    (mg/get-db connection "purui_new")))



 ;(j/query db-spec ["select count(*) from news where"])



 (defn sql-to-mongo
   ([sql-spec sql-table mongo-spec mongo-collection sql-snippet]
    (let [count (->> (j/query sql-spec [(str "select count(*) from " sql-table sql-snippet)])
                     first
                     vals
                     first)
          first-id (->> (j/query sql-spec [(str "select * from " sql-table sql-snippet " limit 0, 1")])
                        first
                        :id)
          last-id (->> (j/query sql-spec [(str "select * from " sql-table sql-snippet " limit " (- count 1) ", " count)])
                       first
                       :id)
          trunks (partition-all 500 (range count))]
      (doseq [t trunks]
        (let [head (first t)
              tail (+ (last t) 1)
              sql (str "select * from " sql-table sql-snippet " limit " head ", " tail)
              input  (j/query sql-spec [sql])]
          (do
          (mmc/insert-batch mongo-spec mongo-collection input)
            (println (apply count input)))))
      [first-id last-id]))
   ([sql-spec sql-table mongo-spec mongo-collection start-date end-date]
    (let [sql-snippet (str " where publish_time >= '" start-date
                           "' and publish_time < '" end-date "'")
          ids (sql-to-mongo sql-spec sql-table mongo-spec mongo-collection sql-snippet)
          first-id (first ids)
          last-id (second ids)
          sql-snippet2 (str " where id >= " first-id
                            " and id <= " last-id)]
      (sql-to-mongo sql-spec (str sql-table "_html") mongo-spec (str mongo-collection "_html") sql-snippet2))))

(sql-to-mongo sql-spec "news" mongo-spec "news" "2014-6-10" "2014-6-12")


(count (j/query sql-spec ["select * from news where publish_time <= '2014-6-13' limit 0, 1"]))
