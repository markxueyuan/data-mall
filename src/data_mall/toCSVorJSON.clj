(ns data-mall.toCSVorJSON)

(require '[incanter.core :as incanter]
         '[incanter.io :as io]
         '[clojure.data.csv :as csv]
         '[clojure.data.json :as json]
         '[clojure.java.io :as javaio]
         '[data-mall.toMatrix :as datasrc])

(def race-data (io/read-dataset "D:/data/all_160.P3.csv" :header true))

(def census2012 (incanter/$ [:STATE :NAME :POP100 :P003002 :P003003 :P003004 :P003005 :P003006 :P003007 :P003008] race-data))

;(incanter/view census2012)
;(incanter/col-names census2012)

;(incanter/to-list census2012)

(with-open [f-out (javaio/writer "D:/data/census-2012.csv")]
  (csv/write-csv f-out [(map name (incanter/col-names census2012))])
  (csv/write-csv f-out (incanter/to-list census2012)))


#_(->
 (:rows census2012)
 first)

(with-open [f-out (javaio/writer "D:/data/census-2012.json")]
  (json/write (:rows census2012) f-out))



(defn toCSV
  "This writes any datasets into csv files"
  [datasets output]
  (with-open [f-out (javaio/writer output)]
  (csv/write-csv f-out [(map name (incanter/col-names datasets))])
  (csv/write-csv f-out (incanter/to-list datasets)))
  )

(defn toCSV2
  [resultsets output]
   (with-open [f-out (javaio/writer output)]
     (csv/write-csv f-out [(map name (incanter/col-names datasets))])
  (csv/write-csv f-out (incanter/to-list datasets)))
  )

(keys )

(incanter/to-list [{:a 2 :b 3 :c 4} {:a 5 :b 7 :c 9}])

;(require '[data-mall.scrapeTable :as scrapeTable])

;(toCSV scrapeTable/xy "D:/data/PriceIndexForHomeElectric.csv")










