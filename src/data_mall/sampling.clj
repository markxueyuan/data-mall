(ns data-mall.sampling
  (:require [clojure.java.jdbc :as jdbc]))

;;;;;;;;;;;;;;db-connection;;;;;;;;;;;;;;;;;

(def db-spec1
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//localhost:3306/doubancomment"
   :user "root"
   :password "othniel"})


;;;;;;;;;;;;;;fns;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sample-percent
  [percent coll]
  (filter (fn [_] (<= (rand) percent)) coll))


;;;;;;;;;;;;;;sqls;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def query-1 "SELECT * FROM dfl;")

;;;;;;;;;;;;;;;working area;;;;;;;;;;;;;;;;;;

#_(->> (jdbc/query db-spec1 [query-1])
     (sample-percent 0.1)
     count
     )

