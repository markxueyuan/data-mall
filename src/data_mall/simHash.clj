(ns data-mall.simHash
  (:use [simhash.core :as simhash]
        [clojure.string :only (split)]))

(md5 "学")
(md5 "我就不相信科学呀")



(sort < (map hash (map str (seq "我就不相信科学那简直是一定的"))))
(sort < (map hash (map str (seq "我就不相信科学那简直是一定的了"))))

(take 5 (sort < (map hash (map str (seq "我爱北京天安门")))))


(byte 123)

format

(count "08e2dff0b511ef23c7322fdc590a9ec5")
(count "3895e642913d57daf014162635ece519")

(defn simhash
  [string]

  )

(Integer/toBinaryString 25105)




