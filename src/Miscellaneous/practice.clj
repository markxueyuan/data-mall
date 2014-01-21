(require '[data-mall.connectDB3 :as db])


(def db-spec (db/mysql-connector "database"))

(def b (lazy-seq (db/load-table-data db-spec "initial12161231")))

(doall
 (data-mall.toCSVorJSON/toCSV b "D:/data/aaaaa"))




