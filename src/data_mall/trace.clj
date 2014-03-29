(ns data-mall.trace
  (:require [clojure.tools.trace :as t]))

(t/trace-ns *ns*)

(t/trace (* 2 3))

(t/deftrace fubar [x v] (+ x v))

(fubar 3 4)

(+ 3 4)

(t/trace-forms (+ 1 3) (* 5 6) (/ 0 1))
