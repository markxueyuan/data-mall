(ns data-mall.datasets)

(require '[incanter.core :as incanter]
         '[incanter.io :as io]
         '[incanter.datasets :as datasets])

(def iris (datasets/get-dataset :iris))

(incanter/view iris)

(incanter/col-names iris)

(incanter/nrow iris)

(-> (incanter/$ :Species iris)
    set
    )










