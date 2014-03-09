(ns Miscellaneous.baike
  (:use clojure.java.io
        [clojure.string :only (lower-case)])
  (:require [net.cgrand.enlive-html :as enlive]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all])
  (:import (java.net URL MalformedURLException)
           java.util.concurrent.LinkedBlockingQueue
           [com.mongodb MongoOptions ServerAddress WriteConcern]
           org.bson.types.ObjectId))

(mg/connect!)

(mg/set-db! (mg/get-db "plant"))




(defn base-inf-from
  [html]
  (let [base (enlive/select html [:div.baseInfoWrap :div.biItemInner])
        title (->> (enlive/select base [:span.biTitle])
                   (map :content))
        content (->> (enlive/select base [:div.biContent])
                     ;(map :content)
                     identity)]
    content))


(base-inf-from (enlive/html-resource
                                             (URL. "http://baike.baidu.com/subview/4144/5380320.htm?fromId=4144&from=rdtself")))

(mc/insert-batch "baseInfo"
                 (base-inf-from (enlive/html-resource
                                             (URL. "http://baike.baidu.com/subview/4144/5380320.htm?fromId=4144&from=rdtself"))))

