(ns language.paraIncanter
  (:require (incanter [core :as incanter]
                      [charts :as charts]
                      [stats :as stats]
                      [datasets :as datasets]
                      [io :as io]
                      [optimize :as opt])))



(def data-file "D:/data/all_160_in_51.p35.csv")




(def data (incanter/to-matrix (incanter/sel (io/read-dataset data-file :header true)
                                            :cols [:POP100 :HU100])))

(def population (incanter/sel data :cols 0))

(def housing-units (incanter/sel data :cols 1))

(def ml (stats/linear-model housing-units population))

;(def plot (scatter-plot population housing-units :legend true))
