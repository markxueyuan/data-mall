(ns data-mall.importCSV)

(require '[incanter.core :as incanter]
         '[incanter.io :as io])

(def bb (io/read-dataset "D:/data/small-sample.csv"))




(def cc (io/read-dataset "D:/data/small-sample-header.csv" :header true))

(incanter/view cc)


(def dd (io/read-dataset "D:/data/all_160_in_51.P35.csv" :header true))

(incanter/view dd)





;;;;;;;;;;;;;;;;not that smart;;;;;;;;;;;;;;;;;;

;(def bb (io/read-dataset "D:/data/small-sample.csv"))

;(def cc (io/read-dataset "D:/data/small-sample-header.csv" :header true))

;(incanter/view cc)

;(def dd (io/read-dataset "D:/data/all_160_in_51.P35.csv" :header true))

;(incanter/view dd)


