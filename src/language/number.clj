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

(with-precision 2 (/ 101M 3))


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
     ))

;numbers of same cateogry are equal, although they have different types.

(= 1 1N (Integer. 1) (Short. (short 1)) (Byte. (byte 1)))


;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;

;if you want to instant a data type class, you have to give it a correspondent primitive type first

#_(short. 1) ;will make an error

;however, different categories cannot equal

(= 1 1.0)
(= 1N 1M)
(= 1.25 5/4)

;numerical equivalence


(== 1 1.0)
(== 1N 1M)
(== 1.25 5/4)

(defn equiv?
  [& args]
  (and (every? number? args)
       (apply == args)))

(equiv? "foo" "bar")

;;clojure collections use numerical equals

(into #{} [1 1N (Integer. 1) (Short. (short 1))])

(into {}
      [[1 :long]
       [1N :bigint]
       [(Integer. 1) :integer]])

;;;;pay attention!
(+ 0.1 0.2)

(double (float 1.1))

(= 1.1 (float 1.1))

(defn foo [a] 0)

(class foo)
(seq (.getDeclaredMethods (class foo)))






