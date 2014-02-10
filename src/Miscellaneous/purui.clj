(ns Miscellaneous.purui
  (:require [data-mall.lazyProcess :as lz]
            [incanter.core :as incanter]
            [clojure.java.jdbc :as jdbc]
            [data-mall.toCSVorJSON :as toCSV]))

(def db-spec
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

(jdbc/query db-spec2 ["select * from previews"]
            :row-fn :similar_count
            :result-set-fn frequencies)

(toCSV)





(reduce  + (Integer. "3") [1 2 3])



;(map :pubtime (lz/lazy-read-db db-spec2 'previews))



;(frequencies (map :text (lz-db/lazy-read-db db-spec 'previews)))


;(lz/lazy-read-csv "D:/data/previews.csv")
;(def data (drop 1 (lz/lazy-read-csv "D:/data/previews.csv")))

;(count (map #(zipmap [:origin :preview :pubtime :similar_count :title :url] %) data))

