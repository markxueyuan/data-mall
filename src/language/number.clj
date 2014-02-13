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

