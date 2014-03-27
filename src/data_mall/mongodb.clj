(ns data-mall.mongodb
  (:refer-clojure :exclude [sort find])
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.result :as mr]
            [validateur.validation :refer :all]
            ;[clojure.test :refer [is]]
            [monger.multi.collection :as mmc];mirror of mc, but with db as first argument for every function.
            [monger.conversion :refer [from-db-object]]
            [monger.query :refer :all :as mq]
            )
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern BasicDBObject BasicDBList]
           org.bson.types.ObjectId
           java.util.ArrayList))

;connect to db

(mg/connect!)

#_(mg/set-db! (mg/get-db "Iamsinger"))

(mg/set-db! (mg/get-db "test"))

#_(def data (mc/find-maps "fulltime"))


;insert into db

#_(mc/insert-and-return "basics" {:name "John" :age 30})

#_(mc/insert "basics" {:_id (ObjectId.) :first_name "Jimmy" :last_name "Newton"})

#_(mc/insert "basics" {:_id 128796 :first_name "良" :last_name "张"})

#_(mc/insert-and-return "basics" {:_id (ObjectId.) :name "貂蝉" :property "beauty"})

#_(mc/insert-batch "basics" [{:_id (ObjectId.) :name "瓜瓜" :property "官二代"} {:_id (ObjectId.) :name "康师傅" :property "Uknown"}])

#_(let [dbb (mg/get-db "newnew")]
  (mc/insert-and-return dbb "hehe" {:name "唐飞燕" :dynasty "唐"} WriteConcern/NORMAL))

;insert array

#_(mc/insert "insertarray" {:name "John" :age 30 :pets ["sam" "chelsie"]})



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
(mc/find-one "insertarray" {:name "John"})
(mc/find-one-as-map "insertarray" {:name "John"})
(mc/find-one "insertarray" {:name "John"} ["name" "age"])
(mc/find-one "insertarray" {:name "John"} {:name 1 :age 1})

(mmc/find-maps (mg/get-db "temperature") "temperature" {"d.x" 3} {"d.x" 1 :_id 0})


(let [oid (ObjectId.)]
  (mc/insert "insertarray" {:_id oid :first_name "John" :last_name "Lennon"})
  (mc/find-map-by-id "insertarray" oid))

;ObjectId

#_(let [oid (ObjectId.)
      entry {:a 2 :b 3}]
  (mc/insert-and-return "basics" (merge entry {:_id oid})))

#_(ObjectId. "5312dd1a3df359d5919cc5c6")

(str (ObjectId. "5312dd1a3df359d5919cc5c6"))

;conversion

(from-db-object (mc/find "insertarray" {:name "John"}) true)


;query operators

$gt
$gte
$lt
$lte
;$ is macro expanded during compiling
;these operators work on dates, can be java.util.Date or Joda Time(clj-time)

(mmc/find-maps (mg/get-db "temperature") "temperature" {:x {"$gt" 2 "$lte" 10}})
(mmc/find-maps (mg/get-db "temperature") "temperature" {:x {$gt 2 $lte 10}})

$exists

(mmc/find-maps (mg/get-db "temperature") "temperature" {"d.x" {$exists false}})

$mod

(mmc/find-maps (mg/get-db "temperature") "temperature" {:x {$mod [3 2]}})

$ne

(mmc/find-maps (mg/get-db "temperature") "temperature" {:x {$ne 5}})
(mmc/co  unt (mg/get-db "temperature") "temperature" {:x {$ne 5}})

$all
$in
$nin

(mmc/find-maps (mg/get-db "temperature") "temperature" {:j {$all [1 2 3 ]}} {:j 1 :_id 0})

(mmc/find-maps (mg/get-db "temperature") "temperature" {:z {$in [5 6 7]}} {:z 1 :_id 0})

(mmc/find-maps (mg/get-db "temperature") "temperature" {:x {$nin [1 2 3 4]}} {:x 1 :_id 0})


$and
$or
$nor

(mmc/find-maps (mg/get-db "temperature") "temperature" {$and [{:x 2} {"d.x" 3}]} {:x 1 :d 1 :_id 0})
(mmc/find-maps (mg/get-db "temperature") "temperature" {$or [{:x 2} {"d.x" 3}]} {:x 1 :d 1 :_id 0})
(mmc/find-maps (mg/get-db "temperature") "temperature" {$nor [{:x 2} {"d.x" 3}]} {:x 1 :d 1 :_id 0})


$regex

;options ignore upper case or lower case
(mmc/find-maps (mg/get-db "temperature") "temperature" {:x {$regex "Gra.*" $options "i"}} {:x 1 :_id 0})

$elemMatch
(mmc/find-maps (mg/get-db "temperature") "temperature" {:d {$elemMatch {:x 444 :y {$lte 11}}}})

;getting distinct documents

(mmc/distinct (mg/get-db "temperature") "temperature" :x)


;query dsl

(mg/with-db (mg/get-db "temperature")
            (with-collection "temperature"
              (find {})
              (fields [:x :y])
              (sort (array-map :x -1 :y 1))))

;update

#_(mc/update "stars" {:Name "Lei Han"} {:Ethics "Inner Mongolian"})
#_(mc/update "stars" {:_id (ObjectId. "53119f7adae6dc0e29320928")} {$set {:Name "dashu"}})
#_(mc/update "stars" {:Name "Lei Han"} {$set {:Location "Beijing"}})

;remove

#_(mc/insert-batch "stars" [{:Name 1 :List "ke"} {:Name 2 :List "ke"} {:Name 2 :List "ke"}])

#_(mc/remove "stars" {:List "ke"})
