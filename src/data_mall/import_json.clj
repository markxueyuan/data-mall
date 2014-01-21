(ns data-mall.import-json)

(require '[incanter.core :as incanter])
(require '[clojure.data.json :as json])

(incanter/to-dataset (json/read-json (slurp "D:/data/small-sample.json")))

