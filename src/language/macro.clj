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




















