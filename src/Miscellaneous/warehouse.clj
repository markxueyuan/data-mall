(ns Miscellaneous.shzh
  (:refer-clojure :exclude [sort find])
  (:require [clojure.string :as string]
            [incanter.core :as incanter]
            [data-mall.connectDB3 :as db]
            [clojure.java.jdbc :as jdbc]
            [monger.core :as mg];the following 4 is for mongo use
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.query :refer :all]
            [Miscellaneous.purui :as purui])
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern];the following two is for mongo use
           org.bson.types.ObjectId))

;(mg/connect! {:host "192.168.3.53" :port 7017})

;(mg/set-db! (mg/get-db "test"))

;(mc/find-maps "star_baidunews_history")

#_(with-collection "star_baidunews_history"
  find{})
(mg/connect!)
(mg/set-db! (mg/get-db "purui"))
(pmap #(mc/insert-batch "fulltime" %) (partition-all 50 purui/set6))

