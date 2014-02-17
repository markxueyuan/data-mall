(ns Miscellaneous.purui
  (:require [data-mall.lazyProcess :as lz]
            [incanter.core :as incanter]
            [clojure.java.jdbc :as jdbc]
            [data-mall.toCSVorJSON :as toCSV]
            [data-mall.ansj-seg :as word-seg]
            [clojure.java.io :as javaio]
            [clojure.string :as string]
            [clj-time.core :as t]
            [clj-time.format :as tformat]
            ))

;;;;;;;;;;;time format;;;;;;;;;;

(def formatter1 (tformat/formatters :mysql))
(def formatter2 (tformat/formatter "yy/MM/dd"))
(def formatter3 (tformat/formatter "yy-MM-dd"))




;;;;;;;;;;;db connection;;;;;;;;

(def db-spec1
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//localhost:3306/doubancomment"
   :user "root"
   :password "othniel"})

(def db-spec2
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//localhost:3306/purui"
   :user "root"
   :password "othniel"})

;;;;;;;;;;;functions;;;;;;;;

(defn ->csv
  [file resultset]
  (toCSV/toCSV (incanter/to-dataset resultset) file))

(defn cut-date-string
  [date-string]
  (first (string/split date-string #"\s")))

(defn extract-date
  [identifier entry]
  (let [tkey (keyword identifier)]
    (assoc entry tkey (cut-date-string (tkey entry)))))


(defn sort-column
  [last-key entry]
  (let [results entry]
    (into (sorted-map-by (fn [key1 key2]
                           (if (= key1 last-key)
                             1
                             -1)))
          results)))

(defn correct-nil
  [stuff col-key entry]
  (if (or (= "" (col-key entry))(nil? (col-key entry)))
    (assoc entry col-key stuff)
  entry))

(defn cp2
  [set1 set2 key1 key2 k-1 k-2]
  (let [c (fn [key s1 s2] (compare (key (first s1)) (key (first s2))))]
    (loop [s1 set1 s2 set2 col []]
      (let [e1 (first s1) e2 (first s2)]
            (if (or (nil? e1) (nil? e2))
              col
              (cond (> (c key1 s1 s2) 0) (recur s1 (rest s2) (conj col (assoc e2 k-1 0)))
                    (< (c key1 s1 s2) 0) (recur (rest s1) s2 (conj col (assoc e1 k-2 0)))
                    (> (c key2 s1 s2) 0) (recur s1 (rest s2) (conj col (assoc e2 k-1 0)))
                    (< (c key2 s1 s2) 0) (recur (rest s1) s2 (conj col (assoc e1 k-2 0)))
                    (= (c key2 s1 s2) 0) (recur (rest s1) (rest s2) (conj col (into e1 e2)))))))))

(defn cp1
  [set1 set2 key k-1 k-2]
  (let [c (fn [key s1 s2] (compare (key (first s1)) (key (first s2))))]
    (loop [s1 set1 s2 set2 col []]
      (let [e1 (first s1) e2 (first s2)]
            (if (or (nil? e1) (nil? e2))
              col
              (cond (> (c key s1 s2) 0) (recur s1 (rest s2) (conj col (assoc e2 k-1 0)))
                    (< (c key s1 s2) 0) (recur (rest s1) s2 (conj col (assoc e1 k-2 0)))
                    (= (c key s1 s2) 0) (recur (rest s1) (rest s2) (conj col (into e1 e2)))))))))





;;;;;;;;csv file address ;;;;;;;

(def address-1 "E:/数据/朴睿/analysis.csv")
(def address-2 "D:/data/analysis.csv")

;;;;;;zone of sqls;;;;;;;

(def query-1 "select topic, count(*) as vol from purui0208_2013_spam_dupliremov group by topic;")
(def query-2 "select kw, topic, count(*) as vol from purui0208_2013_spam_dupliremov group by kw order by topic, kw;")
(def query-3 "select pubtime FROM purui0208_2013_spam_dupliremov")
(def query-4 "SELECT time FROM dfl;")
(def query-5 "SELECT score,vote FROM dfl;")
(def query-6 "SELECT topic, kw, preview FROM purui0208_2013_spam_dupliremov;")
(def query-7 "SELECT * FROM purui0208_2013_spam_dupliremov;")
(def query-8 "SELECT brand,pubtime FROM purui0208_2013_spam;")
(def query-9 "SELECT brand, pubtime FROM purui0214_2013_spam_dupliremov_media;")
(def query-10 "SELECT origin FROM purui0208_2013_spam;")
(def query-11 "SELECT origin FROM purui0214_2013_spam_dupliremov_media;")



;;;;;;;;;working area;;;;;;;


;group by topic

#_(->> (jdbc/query db-spec2 [query-1])
     (->csv address-1)
     )

;group by keyword

#_(->> (jdbc/query db-spec2 [query-2])
     (->csv address-1)
     )

;distribution by time



#_(->> (jdbc/query db-spec2 [query-3])
     (map #(extract-date "pubtime" %))
     frequencies
     (map #(assoc (first %) :counts (second %)));mimic a standard jdbc output
     (sort #(compare (tformat/parse formatter2 (:pubtime %1)) (tformat/parse formatter2 (:pubtime %2))));parse the time to compare
     ;(map #(sort-column :counts %))
     ;(->csv address-1);write to csv
     ;(incanter/to-dataset)
     ;(incanter/view)
     ;(incanter/to-list)
     ;first
     ;(sort-column :counts)
     (toCSV/toCSV2 [:pubtime :counts] address-1)
     )

;why not forget incanter?
#_(->> (jdbc/query db-spec1 [query-4])
     frequencies
     (map #(assoc (first %) :counts (second %)))
     (map #(correct-nil "1900-1-1" :time %))
     (sort #(compare (tformat/parse formatter3 (:time %1)) (tformat/parse formatter3 (:time %2))))
     (toCSV/toCSV2 [:time :counts] address-2)
     )

;calculate the word frequencies

#_(->> (jdbc/query db-spec2 [query-6])
     (map #(correct-nil "uk" :preview %))
     (map #(word-seg/word-seg :preview %))
     (mapcat :word-seg)
     (map #(correct-nil "uk" :nature %))
     (map #(correct-nil "uk" :word %))
     (remove #(> 2 (count (:word %))))
     frequencies
     (map #(assoc (first %) :counts (second %)))
     (sort #(> (:counts %1) (:counts %2)))
     (toCSV/toCSV2 [:word :nature :counts] address-1)
     )

;merge time tendency of raw data and clean data
(def set1
  (->> (jdbc/query db-spec2 [query-8])
       (map #(extract-date "pubtime" %))
       frequencies
       (map #(assoc (first %) :all-counts (second %)))
       (map #(correct-nil "1900-1-1" :pubtime %))
       (sort-by (juxt :brand :pubtime))
     ))

set1

(def set2
  (->> (jdbc/query db-spec2 [query-9])
       (map #(extract-date "pubtime" %))
       frequencies
       (map #(assoc (first %) :unique-counts (second %)))
       (map #(correct-nil "1900-1-1" :pubtime %))
       (sort-by (juxt :brand :pubtime))
     ))
set2




(->> (cp2 set1 set2 :brand :pubtime :all-counts :unique-counts)
     (toCSV/toCSV2 [:brand :pubtime :all-counts :unique-counts] address-1)
     )

;merge origin distribution of raw data and clean data

(def set3
  (->> (jdbc/query db-spec2 [query-10])
       frequencies
       (map #(assoc (first %) :all-counts (second %)))
       (sort-by :origin)
     ))

set3

(def set4
  (->> (jdbc/query db-spec2 [query-11])
       frequencies
       (map #(assoc (first %) :unique-counts (second %)))
       (sort-by :origin)
     ))

set4

(->> (cp1 set3 set4 :origin :all-counts :unique-counts)
     (sort #(> (:all-counts %1) (:all-counts %2)))
     (toCSV/toCSV2 [:origin :all-counts :unique-counts] address-1)
     )
(cp1 set3 set4 :origin :all-counts :unique-counts)







;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;;;;



;do calculation lazily

#_(jdbc/query db-spec2 ["select * from previews"]
            :row-fn :similar_count
            :result-set-fn frequencies)


;return vector

#_(def a (jdbc/query db-spec2 ["SELECT origin, count(*) from previews group by origin order by count(*) desc;"]
                     :as-arrays? true))


;insert rows
#_(jdbc/insert! db-spec2 :previews {:origin "大纪元" :preview "她是个好同志"})

;update rows
#_(jdbc/update! db-spec2 :previews {:preview "她是个大坏蛋" :title "鬼吹灯系列"} ["origin = ?" "大纪元"])

;delete rows

#_(jdbc/delete! db-spec2 :previews ["origin = ?" "大纪元"])

;rename col name
#_(jdbc/query db-spec2 ["select * from previews limit 20;"]
            :identifiers #(.replace % \_ \-)
            )


;;;;;;;;;;;;;;;;;;;;;;;;;;not that smart;;;;;;;;;;;;;;;;;;;;;;;;
(string/split "1012-3-4 12:22" #"\s")


