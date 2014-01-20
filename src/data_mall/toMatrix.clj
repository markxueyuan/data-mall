(ns data-mall.toMatrix)

(require '[incanter.core :as incanter]
         '[incanter.io :as io])

(def virginia (io/read-dataset "D:/data/all_160_in_51.P35.csv" :header true))

(incanter/view virginia)

(->> virginia
     (incanter/$ [:POP100 :HU100 :P035001])
     incanter/to-matrix
     first
     )

(->> virginia
     (incanter/$ [:POP100 :HU100 :P035001])
     incanter/to-matrix
     (take 5)
     )

(->> virginia
     (incanter/$ [:POP100 :HU100 :P035001])
     incanter/to-matrix
     count)

(def va-matrix
  (->> virginia
     (incanter/$ [:POP100 :HU100 :P035001])
     incanter/to-matrix))

(reduce incanter/plus va-matrix)






