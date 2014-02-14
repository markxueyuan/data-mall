(ns language.number)

;since floating data are sinful
(+ 0.1 0.1 0.1)
;we try rationals
(+ 1/10 1/10 1/10)
;you can coerce a rational into float
(double 1/3)
;you can make a float into a rational
(rationalize 0.244532)
;rules of numeric contagion
(+ 1 1.5)
(+ 1 1N)
(+ 1.3 1/3)
(+ 0.3 0.3M)

(defn squares-sum
  [& vals]
  (reduce + (map * vals vals)))

(squares-sum 1 3 5)

(def k Long/MAX_VALUE)

k
(inc k)
(inc (bigint k))
(def l Double/MAX_VALUE)
l
(* 100 (bigdec l))
(* 0.5M 1e403M)

;autopromoting

(inc' k)

(inc' 1)

;unchecked-*

(unchecked-dec (unchecked-inc Long/MAX_VALUE))

;rounding

(with-precision 300 (/ 22M 3))


;object identity
(identical? "foot" (str "fo" "ot"))

(let [a (range 10)]
  (identical? a a))

(identical? 5/4 (+ 3/4 1/2))

(identical? 5.4321 5.4321)

;dada, miracles happen below,
(identical? 127 127)
;this is called fixnums

;reference equality
(= {:a 1 :b ["hi"]}
   (into (sorted-map) [[:b ["hi"]] [:a 1]])
   (doto (java.util.HashMap.)
     (.put :a 1)
     (.put :b ["hi"])
     )
   )













