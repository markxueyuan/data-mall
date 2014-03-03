(ns Miscellaneous.purui
  (:require [data-mall.lazyProcess :as lz]
            [incanter.core :as incanter]
            [clojure.java.jdbc :as jdbc]
            [data-mall.toCSVorJSON :as toCSV]
            [data-mall.lazyProcess :as fromCSV]
            [data-mall.ansj-seg :as word-seg]
            [clojure.java.io :as javaio]
            [clojure.string :as string]
            [clj-time.core :as t]
            [clj-time.format :as tformat]
            [data-mall.synonym :as synonym]
            [data-mall.connectDB3 :as connectDB3]
            [data-mall.pivot-table :as pt]
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

(def db52 (connectDB3/connect52 "purui"))

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
            (cond (and (nil? e1) (not (nil? e2))) (reduce #(merge %1 %2) col (map #(assoc % k-1 0) s2))
                  (and (not (nil? e1)) (nil? e2)) (reduce #(merge %1 %2) col (map #(assoc % k-2 0) s1))
                  (and (nil? e1) (nil? e2)) col
                  (> (c key1 s1 s2) 0) (recur s1 (rest s2) (conj col (assoc e2 k-1 0)))
                  (< (c key1 s1 s2) 0) (recur (rest s1) s2 (conj col (assoc e1 k-2 0)))
                  (> (c key2 s1 s2) 0) (recur s1 (rest s2) (conj col (assoc e2 k-1 0)))
                  (< (c key2 s1 s2) 0) (recur (rest s1) s2 (conj col (assoc e1 k-2 0)))
                  (= (c key2 s1 s2) 0) (recur (rest s1) (rest s2) (conj col (into e1 e2))))))))

(defn cp1
  [set1 set2 key k-1 k-2]
  (let [c (fn [key s1 s2] (compare (key (first s1)) (key (first s2))))]
    (loop [s1 set1 s2 set2 col []]
      (let [e1 (first s1) e2 (first s2)]
            (cond (and (nil? e1) (not (nil? e2))) (reduce #(merge %1 %2) col (map #(assoc % k-1 0) s2))
                  (and (not (nil? e1)) (nil? e2)) (reduce #(merge %1 %2) col (map #(assoc % k-2 0) s1))
                  (and (nil? e1) (nil? e2)) col
                  (> (c key s1 s2) 0) (recur s1 (rest s2) (conj col (assoc e2 k-1 0)))
                  (< (c key s1 s2) 0) (recur (rest s1) s2 (conj col (assoc e1 k-2 0)))
                  (= (c key s1 s2) 0) (recur (rest s1) (rest s2) (conj col (into e1 e2))))))))

(defn inner-join-1
  [set1 set2 key]
  (let [c (fn [key s1 s2] (compare (key (first s1)) (key (first s2))))]
    (loop [s1 set1 s2 set2 col []]
      (let [e1 (first s1) e2 (first s2)]
            (cond (or (nil? e1) (nil? e2)) col
                  (> (c key s1 s2) 0) (recur s1 (rest s2) col)
                  (< (c key s1 s2) 0) (recur (rest s1) s2 col)
                  (= (c key s1 s2) 0) (recur (rest s1) (rest s2) (conj col (into e1 e2))))))))

(defn csv-key
  [resultset]
  (map keyword (first resultset)))

(defn map-csv
  [resultset]
  (let [csv-key (map keyword (first resultset))]
    (map #(zipmap csv-key %) (rest resultset))))

(defn url-hash
  [url-col entry]
  (let [h (hash (url-col entry))]
    (assoc entry :url-hash h)))

(defn polygamy
  [key1 key2 entry]
  (let [husband (assoc {} key1 (key1 entry))]
    (map #(into husband %) (key2 entry))))


(defn better-db-insert! [db-spec table resultset]
  (let [args (concat [db-spec table] resultset)]
    (apply jdbc/insert! args)))

(defn clean-for-csv
  [key entry]
  (let [rep (string/replace (key entry) #"[\,\n]" "，")]
    (assoc entry key rep)))



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
(def query-12 "SELECT * FROM purui0208_2013_spam;")
#_(connectDB3/create-new-table db-spec2
                             "wordseg"
                             (str "id INT NOT NULL AUTO_INCREMENT,"
                                  "brand TEXT NULL,"
                                  "word TEXT NULL,"
                                  "nature TEXT NULL,"
                                  "counts INT NULL,"
                                  "PRIMARY KEY (id),"
                                  "UNIQUE INDEX id_UNIQUE (id ASC)"))
#_(connectDB3/create-new-table db52
                             "testtesttest"
                             (str "id INT NOT NULL AUTO_INCREMENT,"
                                  "brand TEXT NULL,"
                                  "word TEXT NULL,"
                                  "nature TEXT NULL,"
                                  "counts INT NULL,"
                                  "PRIMARY KEY (id),"
                                  "UNIQUE INDEX id_UNIQUE (id ASC)"
                                  ))

(def query-13 (str "select title,origin,topic,brand,preview,left(pubtime,10) as time,sum(count) as count "
                   "from purui0214_2013_spam_dupliremov_media "
                   "group by brand,origin,left(pubtime,10),title,preview;"))

(def query-14 "SELECT * FROM dfl;")

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
#_(def set1
  (->> (jdbc/query db-spec2 [query-8])
       (map #(extract-date "pubtime" %))
       frequencies
       (map #(assoc (first %) :all-counts (second %)))
       (map #(correct-nil "1900-1-1" :pubtime %))
       (sort-by (juxt :brand :pubtime))
     ))

;set1

#_(def set2
  (->> (jdbc/query db-spec2 [query-9])
       (map #(extract-date "pubtime" %))
       frequencies
       (map #(assoc (first %) :unique-counts (second %)))
       (map #(correct-nil "1900-1-1" :pubtime %))
       (sort-by (juxt :brand :pubtime))
     ))
;set2




#_(->> (cp2 set1 set2 :brand :pubtime :all-counts :unique-counts)
     (toCSV/toCSV2 [:brand :pubtime :all-counts :unique-counts] address-1)
     )

;merge origin distribution of raw data and clean data

#_(def set3
  (->> (jdbc/query db-spec2 [query-10])
       frequencies
       (map #(assoc (first %) :all-counts (second %)))
       (sort-by :origin)
     ))

;set3

#_(def set4
  (->> (jdbc/query db-spec2 [query-11])
       frequencies
       (map #(assoc (first %) :unique-counts (second %)))
       (sort-by :origin)
     ))

;set4

#_(->> (cp1 set3 set4 :origin :all-counts :unique-counts)
     (sort #(> (:all-counts %1) (:all-counts %2)))
     (toCSV/toCSV2 [:origin :all-counts :unique-counts] address-1))

#_(->> (fromCSV/lazy-read-csv "E:/数据/朴睿/everything.csv/everything.csv")
     ;(drop 2000)
     map-csv
     (map #(correct-nil "uk" :extracted %))
     (remove #(= (:extracted %) "uk"))
     ;first
     ;(word-seg/word-seg :extracted)
     (take 20)
     (map (partial url-hash :url))
     (map #(word-seg/word-seg :extracted %))
     (map #(assoc {} :wordseg (:word-seg %) :url-hash (:url-hash %)))

     ;(mapcat :word-seg)
     ;(remove #(> 2 (count (:word %))))
     ;frequencies
     ;(map #(assoc (first %) :counts (second %)))
     ;(sort #(> (:counts %1) (:counts %2)))
     ;dorun
     ;(map :word-seg)
     ;count
     ;(toCSV/toCSV2 [:word :nature :counts] address-1)
     )



#_(->> (jdbc/query db-spec2 [query-12])
     (toCSV/toCSV2 [:origin :topic :brand :kw :pubtime :url] address-1)
     )



#_(def set5 (->> (fromCSV/lazy-read-csv "D:/data/everything.csv")
     map-csv
     (map #(correct-nil "uk" :extracted %))
     (remove #(= (:extracted %) "uk"))
     ;first
     ;(word-seg/word-seg :extracted)
     ;(take 20)
     (map (partial url-hash :url))
     ;(map #(word-seg/word-seg :extracted %))
     ;(map #(assoc {}  :url-hash (:url-hash %)))
     (sort-by :url-hash)
     ;(mapcat :word-seg)
     ;(remove #(> 2 (count (:word %))))
     ;frequencies
     ;(map #(assoc (first %) :counts (second %)))
     ;(sort #(> (:counts %1) (:counts %2)))
     ;dorun
     ;(map :word-seg)
     ;count
     ;(toCSV/toCSV2 [:word :nature :counts] address-1)
     ))

;set5

(def set6 (->> (fromCSV/lazy-read-csv "E:/data/puruifull.csv")
     map-csv
     (map (partial url-hash :url))
     (sort-by :url-hash)
     ))

set6

;write into csv
#_(->> (inner-join-1 set5 set6 :url-hash)
     (take 20)
     (map #(word-seg/word-seg :extracted %))
     (map #(assoc {}  :brand (:brand %) :word-seg (:word-seg %)))
     ;first
     (map #(polygamy :brand :word-seg %))
     (apply concat)
     (remove #(> 2 (count (:word %))))
     frequencies
     (map #(assoc (first %) :counts (second %)))
     (map #(synonym/han :nature %))
     (remove #(= (:nature %) "其他"))
     (remove #(= (:nature %) "数量词"))
     (sort-by (juxt :brand :nature :counts))
     ;(toCSV/toCSV2 [:brand :word :nature :counts] address-1)
     )

;write into db 52



#_(->> (inner-join-1 set5 set6 :url-hash)
     (take 20)
     (map #(word-seg/word-seg :extracted %))
     (map #(assoc {}  :brand (:brand %) :word-seg (:word-seg %)))
     ;first
     (map #(polygamy :brand :word-seg %))
     (apply concat)
     (remove #(> 2 (count (:word %))))
     frequencies
     (map #(assoc (first %) :counts (second %)))
     (map #(synonym/han :nature %))
     (remove #(= (:nature %) "其他"))
     (remove #(= (:nature %) "数量词"))
     (sort-by (juxt :brand :nature :counts))
     (better-db-insert! db52 :testtesttest)
     ;(toCSV/toCSV2 [:brand :word :nature :counts] address-1)
     )

#_(->> (jdbc/query db-spec2 [query-13])
     (juxt :title :origin :topic :brand :pubtime :count)
     (map #(extract-date "pubtime" %))
     frequencies
     (map #(clean-for-csv :title %))
     (toCSV/toCSV2 [:title :origin :topic :brand :pubtime :count] address-1)
     )





#_(pt/pivot-table [:score :time] [:vote :id] [pt/sum pt/list-it] (jdbc/query db-spec1 [query-14]))








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


