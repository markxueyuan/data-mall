(ns data-mall.excel
  (:use clj-excel.core))

(-> (build-workbook (workbook-hssf) {"Numbers" [[1] [2 3] [4 5 6]] "kuaike" [[1] [2] [3] [4] [5]]})
    (save "D:/data/numbers.xls"))

(lazy-workbook (workbook-hssf "D:/data/numbers.xls"))

(def a-cell-value
  {:value "world" :alignment :center
   :border [:none :thin :dashed :thick]
   :foreground-color :grey-25-percent :pattern :solid-foreground
   :font {:color :blue :underline :single :italic true
          :size 12 :font "Arial"}})

(-> (build-workbook (workbook-xssf) {"hello" [[a-cell-value]]})
    (save "D:/data/hello-world.xlsx"))


