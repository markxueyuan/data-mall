(ns Miscellaneous.shzh
  (:refer-clojure :exclude [sort find])
  (:require [clojure.string :as string]
            [net.cgrand.enlive-html :as html]
            [incanter.core :as incanter]
            [data-mall.connectDB3 :as db]
            [clojure.java.jdbc :as jdbc]
            [Miscellaneous.dfl-list :as dfl]
            [monger.core :as mg];the following 4 is for mongo use
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.query :refer :all])
  (:import [java.net URL]
           [com.mongodb MongoOptions ServerAddress WriteConcern];the following two is for mongo use
           org.bson.types.ObjectId))

(mg/connect!)

(mg/set-db! (mg/get-db "crawler"))

(defn trimer
  [input]
  (if (= input nil)
    input
    (string/trim input)))

(let [html (html/html-resource (URL. "http://www.sse.com.cn/market/sseindex/indexlist/s/i000001/const_list.shtml"))
      items (html/select html [:div.block_l1 :table.tablestyle :tr :td :a])
      urls (->> items
                (map #(get % :attrs))
                (map #(get % :href))
                (map #(str "http://www.sse.com.cn" %)))
      content (map :content items)
      strings (map trimer (map first content))
      pairs (map #(string/split % #"\n") strings)
      corp (map #(first %) pairs)
      num  (->> pairs
                (map #(re-seq #"\d+" (second %)))
                (map first)
                (map #(Integer. %)))
      input (map #(zipmap [:corporation :number :url] [%1 %2 %3])  corp num urls)]
  (mc/insert-batch "shangzheng" input))
(Integer/parseInt "123")

(string/trim "\n      a\nb\t          ")
