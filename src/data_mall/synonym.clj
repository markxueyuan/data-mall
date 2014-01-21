(ns data-mall.synonym)
(use '[clojure.string :only (upper-case)])
(require '[data-mall.regex :as regex])

(def state-synonyms
  {"ALABAMA" "AL"
   "ALASKA" "AK"
   "ARIZONA" "AZ"
   "WISCONSIN" "WI"
   "WYOMING" "WI"
   })

(defn normalize-state
  [state]
  (let [uc-state (upper-case state)]
    (state-synonyms uc-state uc-state)))

(map normalize-state ["Alabama" "Ak" "oo"])







;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;little machine to answer any question:

(defn highintl
  [question]
  (let [ans-machine {"1 + 1 =" "2"}]
    (ans-machine question question)))

(highintl "1 + 1 =")










