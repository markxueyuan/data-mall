(ns data-mall.lazyProcess
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as jdbc]))

(defn lazy-read-bad-1
  [csv-file]
  (with-open [in-file (io/reader csv-file)]
    (csv/read-csv in-file)))

(defn lazy-read-bad-2
  [csv-file]
  (with-open [in-file (io/reader csv-file)]
    (doall (csv/read-csv in-file))))

(defn lazy-read-ok
  [csv-file]
  (with-open [in-file (io/reader csv-file)]
    (frequencies (map #(nth % 1) (csv/read-csv in-file)))))

;(lazy-read-ok "D:/data/previews.csv")


(defn lazy-read-csv
  [csv-file]
  (let [in-file (io/reader csv-file)
        csv-seq (csv/read-csv in-file)
        lazy (fn lazy [wrapped]
               (lazy-seq
                (if-let [s (seq wrapped)]
                  (cons (first s) (lazy (rest s)))
                  (.close in-file))))]
    (lazy csv-seq)))

;(lazy-read-csv "D:/data/previews.csv")



(defn lazy-read-db-bad-1
  [db table-name]
  (let [sql (str "select * from " table-name ";")
        con (jdbc/get-connection db)
        db-con (jdbc/add-connection db con)
        db-seq (jdbc/query db-con [sql])
        lazy (fn lazy [wrapped]
               (lazy-seq
                (if-let [s (seq wrapped)]
                  (cons (first s) (lazy (rest s)))
                  (.close con))))]
    (lazy db-seq)))

(defn lazy-read-db
  [db table-name]
  (let [sql-n (str "select count(*) from " table-name ";")
        con (jdbc/get-connection db)
        db-con (jdbc/add-connection db con)
        vol (second (first (first (jdbc/query db-con [sql-n]))))
        sql (fn sql [x] (str "select * from " table-name " limit 1 offset " x ";"))
        query (fn [sql] (first (jdbc/query db-con [sql])))
        s (lazy-seq (take vol (iterate inc 0)))
        lazy (fn lazy [wrapped]
               (lazy-seq
                  (cons (query (sql (first wrapped))) (lazy (rest wrapped)))))]
    (lazy s)))

#_(defn lazy-read-db2
  [db table-name interval]
  (let [sql-n (str "select count(*) from " table-name ";")
        con (jdbc/get-connection db)
        db-con (jdbc/add-connection db con)
        vol (second (first (first (jdbc/query db-con [sql-n]))))
        sql (fn sql [x] (str "select * from " table-name " limit " interval " offset " x ";"))
        query (fn [sql]  (jdbc/query db-con [sql]))
        s (lazy-seq (take (+ 1 (/ vol interval)) (iterate #(+ % interval) 0)))
        lazy (fn lazy [wrapped]
               (lazy-seq
                  (reduce into (query (sql (first wrapped))) (lazy (rest wrapped)))))]
    (lazy s)))









;;;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;

;how to merge two maps together

 (into {:a 2 :b 3} {:c 4 :d 5})






