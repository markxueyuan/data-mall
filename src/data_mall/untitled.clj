(require '[clojure.java.io :as javaio]
         '[clojure.data.csv :as csv]
         '[incanter.io :as io]
         '[incanter.core :as incanter])

(future (apply + (range 1e8)))

(def long-cal (future (apply + (range 1e8))))

@long-cal
@(future (Thread/sleep 5000) :done!)
@long-cal

(apply + (range 1e8))






