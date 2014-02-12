(ns Miscellaneous.purui
  (:require [data-mall.lazyProcess :as lz]
            [incanter.core :as incanter]
            [clojure.java.jdbc :as jdbc]
            [data-mall.toCSVorJSON :as toCSV]
            [clojure.java.io :as javaio]
            [clojure.string :as string]
            [clj-time.core :as t]
            [clj-time.format :as tformat]
            ))

;;;;;;;;;;;time format;;;;;;;;;;

(def formatter1 (tformat/formatters :mysql))
(def formatter2 (tformat/formatter "yy/MM/dd"))




;;;;;;;;;;;db connection;;;;;;;;

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







;;;;;;;;csv file address ;;;;;;;

(def address-1 "E:/数据/朴睿/analysis.csv")

;;;;;;zone of sqls;;;;;;;

(def query-1 "select topic, count(*) as vol from purui0208_2013_spam_dupliremov group by topic;")
(def query-2 "select kw, topic, count(*) as vol from purui0208_2013_spam_dupliremov group by kw order by topic, kw;")
(def query-3 "select pubtime FROM purui0208_2013_spam_dupliremov")


;;;;;;;;;working area;;;;;;;


;group by topic

(->> (jdbc/query db-spec2 [query-1])
     (->csv address-1)
     )

;group by keyword

(->> (jdbc/query db-spec2 [query-2])
     (->csv address-1)
     )

;distribution by time



(->> (jdbc/query db-spec2 [query-3])
     (map #(extract-date "pubtime" %))
     frequencies
     (map #(assoc (first %) :counts (second %)));mimic a standard jdbc output
     (sort #(compare (tformat/parse formatter2 (:pubtime %1)) (tformat/parse formatter2 (:pubtime %2))));parse the time to compare
     ;(map #(sort-column :counts %))
     ;(->csv address-1);write to csv
     ;(incanter/to-dataset)
     ;(incanter/view)
     ;(incanter/to-list)
     first
     (sort-column :counts)
     )





 (into {} (sort-by first {:b 3 :a 2}))





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
