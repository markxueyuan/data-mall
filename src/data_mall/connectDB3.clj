(ns data-mall.connectDB3)

(require '[clojure.java.jdbc :as jdbc]
         '[incanter.core :as incanter]
         '[incanter.io :as io])

(import java.io.IOException)

(def db-spec
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//localhost:3306/collegeadmission"
   :user "root"
   :password "othniel"})


(defn mysql-connector
  [schema]
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname (str "//localhost:3306/" schema)
   :user "root"
   :password "othniel"})

(defn connect52
  [schema]
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname (str "//192.168.3.52:3306/" schema)
   :user "root"
   :password "eura_ds"})

(def sqlite-db
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname "D:/data/db"})

(def aa
  (with-open [con (jdbc/get-connection db-spec)]
    (let [db-con (jdbc/add-connection db-spec con)]
      (jdbc/query db-con ["select * from college where cName = ?" "Berkeley"]))))

;(incanter/view aa)

;(jdbc/query sqlite-db ["select * from vocabs_TB where word = 'commune'"])

#_(def bb
  (jdbc/query db-spec ["select * from student"]))

;(incanter/view bb)

#_(def cc
  (jdbc/query db-spec ["select sName from student where sName = ?" "Amy"]))

#_(def dd
  (jdbc/query db-spec ["select * from student left outer join apply using(sID)"]))

#_(def ee
  (jdbc/insert! db-spec :college {:cName "Renmin" :state "CN" :enrol 50000}))

#_(def ff
  (jdbc/update! db-spec :college {:cName "Peking" :state "CN" :enrol nil} ["cName = ?" "Renmin"]))

#_(def gg
  (jdbc/delete! db-spec :college ["cName = ?" "Peking"]))

;(def hh
  ;(jdbc/execute! db-spec ["insert into student values(999, \"Xue\", 4.0, 50000)"]))

;(def ii
  ;(jdbc/db-do-commands db-spec "create table xuetest2 (val1 int not null, val2 varchar(45), primary key (val1))"))

(defn load-table-data
  "This loads the data from a database table"
  [db table-name]
  (let [sql (str "select * from " table-name ";")]
    (with-open [con (jdbc/get-connection db)]
      (let [db-con (jdbc/add-connection db con)]
        (jdbc/query db-con [sql])))))

#_(load-table-data db-spec 'student)

#_(def xx (load-table-data db-spec 'student))

#_(incanter/to-dataset xx)

#_(incanter/view xx)

(defn create-new-table
  [db table-name fields]
  (let [sql (str "create table " table-name " (" fields ") " "DEFAULT CHARSET=utf8;")]
    (with-open [con (jdbc/get-connection db)]
      (let [db-con (jdbc/add-connection db con)]
        (jdbc/db-do-commands db-con sql)))))

;(create-new-table db-spec "hellwin" "val1 int not null, primary key (val1)")

(defn write-into-table
  [db table-name datasets]
  ;(with-open [con db]
    ;(let [db-con (jdbc/add-connection db con)]
      (doseq [entry (:rows datasets)]
        (try
          (jdbc/insert! db (keyword table-name) entry)
          (catch Throwable e
            ;(.printStackTrace e)
           (println "Fuck you windows!" entry))
          (finally
           #_(println "Fuck you windows!" entry)
           )
        )))




















































