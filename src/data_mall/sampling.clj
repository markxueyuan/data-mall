(ns data-mall.sampling
  (:require [clojure.java.jdbc :as jdbc]))

;;;;;;;;;;;;;;db-connection;;;;;;;;;;;;;;;;;

(def db-spec1
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//localhost:3306/doubancomment"
   :user "root"
   :password "othniel"})

;;;;;;;;;;;;;;sqls;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def query-1 "SELECT * FROM dfl;")


;;;;;;;;;;;;;;fns;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sample-percent
  [percent coll]
  (filter (fn [_] (<= (rand) percent)) coll))

(defn rand-replace
  [m kv]
  (into (dissoc m (rand-nth (keys m))) [kv]))

(defn range-from
  [x]
  (map (partial + x) (range))
  )

(defn sample-amount
  [k coll]
  (->> coll
       (drop k)
       (map vector (range-from (inc k)))
       (filter #(<= (rand) (/ k (first %))))
       (reduce rand-replace
               (into {} (map vector (range k) (take k coll))))
       (sort-by first)
       (map first)
  ))

;;;;;;;;;;;;;;;working area;;;;;;;;;;;;;;;;;;

#_(->> (jdbc/query db-spec1 [query-1])
     ;(sample-percent 0.1)
       (sample-amount 50)
     ;count
     )

(into {:a 2 :b 3} [[:c 4]])



