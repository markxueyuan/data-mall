(ns language.multimethod
  (:require clojure.xml))

(clojure.xml/parse "D:/data/BookStore-noDTD.xml")

(defmulti fill
  "fill a xml/html node with the provided value."
  (fn [node value] (:tag node)))

(defmethod fill :div
  [node value]
  (assoc node :content [(str value)]))

(defmethod fill :input
  [node value]
  (assoc-in node [:attrs :value] (str value)))

(defmethod fill :default
  [node value]
  (assoc node :content [(str value)]))

(fill {:tag :div} "heihei")

(fill {:tag :input} "haha")

(fill {:span :input} "gege")


