(ns data-mall.import-json)

(require '[incanter.core :as incanter])
(require '[clojure.data.json :as json])

;(incanter/to-dataset (json/read-json (slurp "D:/data/small-sample.json")))




;;;;;;;;;;;;;;;;;;


#_(json/read-json (slurp "http://api.worldbank.org/countries/all/indicators/SP.POP.TOTL?format=json"))

(defn chengyu [] (json/read-json (slurp "E:/data/export.json")))

