(ns Miscellaneous.doubanComment
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


(defn dry
  [input]
  (if (= input nil)
    input
    (apply string/trim input)));removes whitespaces from both sides of string

(defn trimer
  [input]
  (if (= input nil)
    input
    (string/trim input)))


(defn commentor
  [comment-item]
  (let [people (first (html/select comment-item [:span.comment-info :a]))
        {address :href} (:attrs people)
        name (apply str (:content people))]
    {:name name
     :address address}))

(defn rating&time
  [comment-item]
  (let [rate (html/select comment-item [:span.comment-info :span])
        score (:title (apply :attrs rate))
        time  (dry (:content (second rate)))]
    {:score score
     :time time}
  ))

(defn text
  [comment-item]
  (let [t (->> (html/select comment-item [:div.comment :p])
               (apply :content)
               first
               trimer
               )]
    {:text t}))

(defn vote
  [comment-item]
  (let [v (->> (html/select comment-item [:div.comment :span.comment-vote :span])
               (apply :content)
               (apply str)
               )]
    {:vote v}))

(defn merge-fields
  [c-i]
  (conj (commentor c-i)
        (rating&time c-i)
        (text c-i)
        (vote c-i)))

(defn load-data
  [html-url]
  (let [html (html/html-resource (URL. html-url))
        items (html/select html [:div.comment-item])]
    (incanter/to-dataset (map merge-fields items))))



;(def xy (load-data "http://movie.douban.com/subject/10833923/comments?start=51&limit=20&sort=time"))

;(incanter/view xy)




;;;;;;;;;;;;;;;;;;;;;;;;write data into mysql;;;;;;;;;;;;;;;

(def db-spec
  (db/mysql-connector "doubancomment"))

(def db-52
  (db/connect52 "xueyuan"))

(def ddl (str "id INT NOT NULL AUTO_INCREMENT, "
              "vote TEXT NULL, "
              "text LONGTEXT NULL, "
              "time TEXT NULL, "
              "score TEXT NULL, "
              "name TEXT NULL, "
              "address TEXT NULL, "
              "PRIMARY KEY (id), "
              "UNIQUE INDEX id_UNIQUE (id ASC)"))

#_(defonce table (db/create-new-table db-spec "dflnew" ddl))

#_(doseq [entry dfl/url-list]
  (->> entry
       load-data
       (db/write-into-table db-spec "dflnew"))
       (Thread/sleep 5000))

#_(defonce table52 (db/create-new-table db-52 "dfl" ddl))


#_(doseq [entry dfl/url-list]
  (->> entry
       load-data
       (db/write-into-table db-52 "dfl")
       ))

;;;;;;;;;;;;;;;;;;;write into mongo;;;;;;;;;;;;;;;;;;;;;

#_(mg/connect!)

#_(mg/set-db! (mg/get-db "crawler"))


#_(let [html (html/html-resource (URL. "http://movie.douban.com/subject/10833923/comments?start=51&limit=20&sort=time"))
      items (html/select html [:div.comment-item])]
  (->> (map merge-fields items)
       vec
       (mc/insert-batch "douban")))

#_(map #(dissoc % :_id)(with-collection "douban"
  (find {})
  (fields [:vote :text :time :score :name :address])))



;;;;;;;;;;;tips;;;;;;;;;;;

(string/trim "\n  2012-3-4\n")



;;;;;;;;;;;;not that smart;;;;;;;;;;;;;;;;







