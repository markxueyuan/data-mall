(ns language.macro
  (:require (clojure [string :as string]
                     [walk :as walk])))

(when (= (nth "abc" 1) \b)
  (println "b")
  \b)

(if (= (nth "abc" 1) \b)
  (do (println "b")
    \b))

;;;;;;;;;;;;;;;;

(let [x 2]
  `(let [y ~x]
     y))

;is basically the same as

(let [x 2]
  (list 'let (vector 'y x) 'y))

;the following two's difference helps understand ~@

(let [x '(1 2 3)]
  `(println ~(map (fn [y] (* y 2)) x) 'done))

(let [x '(1 2 3)]
  `(println ~@(map (fn [y] (* y 2)) x) 'done))

;In this expression, the elements from x are run through map, which doubles their values.
;The output of map is inserted into the list that starts with println and ends with 'done.
;Notice that 'done in the output is still quoted.
;It was read in as data—'done—and output as the same data.

(let [x 42]
  `(let [x 13]
     [x x]))

(let [x 42]
  `(let [x# 13]
     [x x#]))

;let's practice a quick macro

(let [x (+ 42 13)]
  (println '(+ 42 13) "=>" x)
  x)

(defmacro debug
  [expression]
  `(let [value# ~expression]
     (println '~expression "=>" value#)
     (flush)
     value#))

(debug (+ 42 13))

(macroexpand-1 '(debug (+ 42 13)))
;another example

(defmacro trymap
  [v]
  `(map inc ~v))

(trymap [1 2 3])

(macroexpand-1 '(trymap [1 2 3]))


;realize a foreach macro,this basically equals to the function of doseq (but not clojure's for)

(defmacro foreach
  [[sym coll] & body]
  `(loop [coll# ~coll]
     (when-let [[~sym & xs#] (seq coll#)]
       ~@body
       (recur xs#))))


;a macro reverses all the symbols in the form

(defmacro reverse-it
  [form]
  (walk/postwalk #(if (symbol? %)
                    (symbol (string/reverse (name %)))
                    %)
                 form))

(macroexpand-1 '(reverse-it (pam cni (egnar 5))))


;we'd like macro returns list to represent futher calls of functions, forms or macros.

(defmacro hello
  [name]
  (list 'println name))

(macroexpand-1 '(hello "Brian"))

;whereas using list is a natural way, It would be out of control when codes become complicated

;as the following one
(defmacro while
  [test & body]
  (list 'loop []
        (concat (list 'when test) body '((recur)))))

(def a (atom 10))

(while (> @a 3) (do (println @a) (swap! a dec)))

;we use this syntax sugar

(defmacro while
  [test & body]
  `(loop []
     (when ~test
       ~@body
       (recur))))

;syntax quote
(def foo 123)

(list `map `println [foo]);is the same as

`(map println [~foo]);and

`(map println ~[foo])

(list `println (list `keyword (list `str foo)));is the same as

`(println (keyword (str ~foo)))

(let [defs `((def x 123)
             (def y 456))]
  (concat (list `do) defs))

(let [defs '((def x 123)
             (def y 456))]
  `(do ~@defs))
;notice that splice unquote implicitly include a concatenate operation
;the list defs are spliced into the surrounding syntax-quoted list

;a very common idiom

(defmacro foo
  [& body]
  `(do-something
    ~@body))

(macroexpand-1
 `(foo
   (doseq [x [1 2 3]]
     (* x 2))
   :hehe))

;quote again, so that the returned forms by macro won't be evaluated

;macroexpand is a function, any value of it would be evaluated

;quote, unquote, unquote-splicing are all functions.

;we can see what these 'functions' actually do by quoting them to stop the return forms evaluated
'`(map println ~[foo])

;you cannot let a qualified x

(defmacro incorrect-let
  [& body]
  `(let [x 3]
     ~@body))

;(incorrect-let (println ":x" x));cannot be evaluated for you cannot let qualified name.

;we would take some clever roundabout ways to solve this problem

(defmacro unhygiene-let
  [& body]
  `(let [~'x 3]
     ~@body))

;however, this has a macro hygiene problem

(let [x 4]
  (unhygiene-let (println ":x" x)))

;here is the problem, the code calls macro does not know the macro returns form bingding x to another value


(macroexpand-1 '(unhygiene-let (println ":x" x)))


(defmacro hello
  [a]
  `(let [~'x 3]
     (concat [x] [~a])))

(unhygiene-let (println "x:" x))

(eval `(let [~'x 3] ~'(println "x:" x)))

(defmacro abc
  [x]
  (println (name x)))

(symbol? (gensym))

(gensym "hahaha")


(defmacro hygienic
  [& body]
  (let [sym (gensym)]
    `(let [~sym :macro-value]
       ~@body
       ~sym)))

(let [x :important]
  (hygienic (println "x:" x)))

;this makes macro safe, we can use auto-gensym to make it more simple

(defmacro hygeienic
  [& body]
  `(let [x# :macro-value]
     ~@body
     x#))

(let [x :important]
 (hygeienic (println "x:" x)))

;auto-gensym under same syntax quote expand to same actual symbol

(defmacro auto-gensyms
  [& numbers]
  `(let [x# (rand-int 10)]
     (+ x# ~@numbers)))

(auto-gensyms 1 2 3 4)

(macroexpand-1 `(auto-gensyms 1 2 3 4))

;however, to unify gensyms in multiple syntax quote, we go back to use (gensym)

(defmacro our-doto
  [expr & forms]
  (let [obj (gensym "obj")]
    `(let [~obj ~expr]
     ~@(map (fn [[f & args]]
              (list* f obj args))
            forms)
     ~obj)))

(our-doto "It works"
   (println "hahahah")
   (println "wagagaga"))

;user picked names

(defmacro with
  [name & body]
  `(let [~name 5]
     ~@body))

(with kala (+ kala 10))
(with yukun (+ yukun 250))

;double evaluation problem

(defmacro spy
  [x]
  `(do
     (println "spied" '~x ~x)
     ~x))



(spy 3);this is ok
(spy (rand-int 10));this is terrible

(macroexpand-1 '(spy (rand-int 10)))

; to solve this, include a gensym all the time when argument appears more than once

(defmacro spy
  [x]
  `(let [x# ~x]
     (println "spied" '~x x#)
     x#))

(macroexpand-1 '(spy (rand-int 10)))

(spy (rand-int 10))

;double evaluation may hint that your macro done jobs that can be extracted into a function


(defn spy-helper
  [expr value]
  (println "spied" expr value))

(defmacro spy
  [x]
  `(spy-helper '~x ~x))

(spy (rand-int 10))


;;;;;;;;;;;tips;;;;;;;;;;;;

(list* 1 2 [4 5])
(list 1 2 [4 5])


