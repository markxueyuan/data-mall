(ns data-mall.easyParel
  (:require [clojure.java.jdbc :as jdbc]
            [data-mall.lazyProcess :as fromCSV]))



(def a (fromCSV/lazy-read-csv "D:/data/everything.csv"))

;please fill the power of parellism by pmap

(->> (map second a)
       (partition-all 500)
       (pmap (fn [chunk] (doall (map #(re-seq #"赵薇" %) chunk))))
       doall
       (remove nil?)
       time)
