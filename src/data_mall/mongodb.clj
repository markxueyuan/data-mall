(ns data-mall.mongodb
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all])
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern]
           org.bson.types.ObjectId))

;connect to db

(mg/connect!)

#_(mg/set-db! (mg/get-db "Iamsinger"))

(mg/set-db! (mg/get-db "purui"))

(def data (mc/find-maps "fulltime"))


;insert into db

#_(mc/insert-and-return "basics" {:name "John" :age 30})

#_(mc/insert "basics" {:_id (ObjectId.) :first_name "Jimmy" :last_name "Newton"})

#_(mc/insert "basics" {:_id 128796 :first_name "良" :last_name "张"})

#_(mc/insert-and-return "basics" {:_id (ObjectId.) :name "貂蝉" :property "beauty"})

#_(mc/insert-batch "basics" [{:_id (ObjectId.) :name "瓜瓜" :property "官二代"} {:_id (ObjectId.) :name "康师傅" :property "Uknown"}])

#_(let [dbb (mg/get-db "newnew")]
  (mc/insert-and-return dbb "hehe" {:name "唐飞燕" :dynasty "唐"} WriteConcern/NORMAL))

#_(let [oid (ObjectId.)
      entry {:a 2 :b 3}]
  (mc/insert-and-return "basics" (merge entry {:_id oid})))

#_(ObjectId. "5312dd1a3df359d5919cc5c6")

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
