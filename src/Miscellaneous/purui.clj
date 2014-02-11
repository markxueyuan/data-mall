(ns Miscellaneous.purui
  (:require [data-mall.lazyProcess :as lz]
            [incanter.core :as incanter]
            [clojure.java.jdbc :as jdbc]
            [data-mall.toCSVorJSON :as toCSV]
            [clojure.java.io :as javaio]
            [clojure.string :as string]
            ))

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

;;;;;;;;csv file address ;;;;;;;

(def address-1 "D:/data/analysis.csv")

;;;;;;zone of sqls;;;;;;;

(def query-1 "select topic, count(*) as vol from purui0208_2013_spam_dupliremov group by topic;")
(def query-2 "select kw, topic, count(*) as vol from purui0208_2013_spam_dupliremov group by kw order by topic, kw;")
(def query-3 "select 'cartier', pubtime FROM purui0208_2013_spam_dupliremov")


;;;;;;;;;working area;;;;;;;


;group by topic

(->> (jdbc/query db-spec2 [query-1])
     (->csv address-1)
     )

;group by keyword

(->> (jdbc/query db-spec2 [query-2])
     (->csv address-1)
     )

;cartier's distribution by time



(->> (jdbc/query db-spec2 [query-3])
     (map #(extract-date "pubtime" %))
     frequencies
     (map #(assoc (first %) :counts (second %)));mimic a standard jdbc output
     (sort #(compare (:pubtime %1) (:pubtime %2)))
     (->csv address-1)
     )



(jdbc/query db-spec2 [query-3])


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
