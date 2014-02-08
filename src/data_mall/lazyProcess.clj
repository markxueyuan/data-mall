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

(lazy-read-ok "D:/data/previews.csv")


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

(lazy-read-csv "D:/data/previews.csv")



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
        vol (jdbc/query db-con [sql-n])]
    (loop [x 0 u nil]
      (let [sql (str "select * from " table-name " limit 1 offset " x ";")
            db-seq (jdbc/query db-con [sql])]
        (if (>= x vol)
          (.close con)
          (let [m (lazy-seq (cons db-seq u))]
            (recur (+ x 1) m)))))))

(str "a " 3)

(lazy-seq (cons 4 (lazy-seq (cons 2 (cons 1 nil)))))




