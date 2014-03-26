(ns data-mall.mongodb
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.result :as mr]
            [validateur.validation :refer :all]
            ;[clojure.test :refer [is]]
            [monger.multi.collection :as mmc];mirror of mc, but with db as first argument for every function.
            )
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern BasicDBObject BasicDBList]
           org.bson.types.ObjectId
           java.util.ArrayList))

;connect to db

(mg/connect!)

#_(mg/set-db! (mg/get-db "Iamsinger"))

(mg/set-db! (mg/get-db "test"))

(def data (mc/find-maps "fulltime"))


;insert into db

#_(mc/insert-and-return "basics" {:name "John" :age 30})

#_(mc/insert "basics" {:_id (ObjectId.) :first_name "Jimmy" :last_name "Newton"})

#_(mc/insert "basics" {:_id 128796 :first_name "良" :last_name "张"})

#_(mc/insert-and-return "basics" {:_id (ObjectId.) :name "貂蝉" :property "beauty"})

#_(mc/insert-batch "basics" [{:_id (ObjectId.) :name "瓜瓜" :property "官二代"} {:_id (ObjectId.) :name "康师傅" :property "Uknown"}])

#_(let [dbb (mg/get-db "newnew")]
  (mc/insert-and-return dbb "hehe" {:name "唐飞燕" :dynasty "唐"} WriteConcern/NORMAL))

;insert array

(mc/insert "insertarray" {:name "John" :age 30 :pets ["sam" "chelsie"]})

;ObjectId

#_(let [oid (ObjectId.)
      entry {:a 2 :b 3}]
  (mc/insert-and-return "basics" (merge entry {:_id oid})))

#_(ObjectId. "5312dd1a3df359d5919cc5c6")

;insert instance of DBObject

#_(let [db-obj (doto (BasicDBObject.)
               (.put "int" 101)
               (.put "dblist" (doto (BasicDBList.)
                                (.put "0" 0)
                                (.put "1" 1)))
               (.put "list" (ArrayList. ["red" "green" "blue"])))]
  (mc/insert "dbobject" db-obj))


;insert-by-part

(defn insert-by-part
  [collection data]
  (let [parts (partition-all 500 data)]
    (map #(mc/insert-batch collection %) parts)))

(defn insert-by-part2
  [db collection data]
  (let [parts (partition-all 500 data)]
    (map #(mc/insert-batch db collection % WriteConcern/NORMAL) parts)))

(defn insert-other-db
  [db collection data]
  (let [archive-db (mg/get-db db)]
    (insert-by-part2 archive-db collection data)))

;check insertion results

#_(let [response (mc/insert-batch "doucument" [{:a 2} {:a 2 :b 3} {:a 2 :b 3 :c 4}])]
  (println (mr/ok? response))
  (println (mr/has-error? response)))

;validation

(let [v (validation-set
         (presence-of [:name :last])
         (presence-of :age))
      result [(v {:name {:first "Joe" :last "wang"} :age 28}) (v {:name "jimmy" :age 28})]
      report (map valid? result)]
  report)

;write concern

;(mg/set-default-write-concern! WriteConcern/FSYNC_SAFE)

;; for data that you can afford to lose
#_(mc/insert "events" {:type "pages.view" :url "http://megastartup.com/photos/9b50311c"} WriteConcern/NORMAL)


;; for data that you absolutely cannot afford to lose and want to be replicated
;; before the function call returns
#_(mc/insert "accounts" {:email "joe@example.com" :password_hash "..." :password_salt "..."} WriteConcern/REPLICAS_SAFE)

;find

#_(mc/find "basics" {:property true})
#_(mc/find-maps "basics" {:property "官二代"})
#_(mc/find-maps "stars" {:Location "Beijing"})

;update

#_(mc/update "stars" {:Name "Lei Han"} {:Ethics "Inner Mongolian"})
#_(mc/update "stars" {:_id (ObjectId. "53119f7adae6dc0e29320928")} {$set {:Name "dashu"}})
#_(mc/update "stars" {:Name "Lei Han"} {$set {:Location "Beijing"}})

;remove

#_(mc/insert-batch "stars" [{:Name 1 :List "ke"} {:Name 2 :List "ke"} {:Name 2 :List "ke"}])

#_(mc/remove "stars" {:List "ke"})
