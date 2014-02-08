(ns Miscellaneous.purui
  (:require [data-mall.lazyProcess :as lz-db]))

(def db-spec
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//localhost:3306/purui"
   :user "root"
   :password "othniel"})

(lz-db/lazy-read-db db-spec 'previews)



