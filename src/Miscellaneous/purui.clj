(ns Miscellaneous.purui
  (:require [data-mall.lazyProcess :as lz-db]
            [incanter.core :as incanter]))

(def db-spec
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//localhost:3306/doubancomment"
   :user "root"
   :password "othniel"})

(frequencies (map :text (lz-db/lazy-read-db db-spec 'dfl)))


(cons {:3 4} nil)



