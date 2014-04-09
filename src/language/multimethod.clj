(ns language.multimethod
  (:require clojure.xml))

(clojure.xml/parse "D:/data/BookStore-noDTD.xml")

(ns-unmap *ns* 'fill)

(defn- fill-dispatch
  [node value]
  (if (= :input (:tag node))
    [(:tag node) (-> node :attrs :type)]
    (:tag node)))

(defmulti fill
  #'fill-dispatch
  :default nil)

;Notice that in our example above, we ns-unmap fill from our namespace
;so we can redefine it. This isn’t typically necessary when redefining
;functions, but defmulti has defonce semantics, so dispatch functions
;cannot be changed without unmapping the root var of the multimethod
;first. This means you have to unmap it from the current namespace
;before redefining it, or your changes will be silently ignored!Notice that in our example above, we ns-unmap fill from our namespace
;so we can redefine it. This isn’t typically necessary when redefining
;functions, but defmulti has defonce semantics, so dispatch functions
;cannot be changed without unmapping the root var of the multimethod
;first. This means you have to unmap it from the current namespace
;before redefining it, or your changes will be silently ignored!



(defmethod fill nil
  [node value]
  (assoc node :content [(str value "haha")]))

(defmethod fill [:input nil]
  [node value]
  (assoc-in node [:attrs :value] (str value)))

(defmethod fill [:input "hidden"]
  [node value]
  (assoc-in node [:attrs :value] (str value)))

(defmethod fill [:input "text"]
  [node value]
  (assoc-in node [:attrs :value] (str value)))

(defmethod fill [:input "radio"]
  [node value]
  (if (= value (-> node :attrs :value))
    (assoc-in node [:attrs :checked] "checked")
    (update-in node [:attrs] dissoc :checked)))

(defmethod fill [:input "checkbox"]
  [node value]
  (if (= value (-> node :attrs :value))
    (assoc-in node [:attrs :checked] "checked")
    (update-in node [:attrs] dissoc :checked)))

(defmethod fill :default
  [node value]
  (assoc-in node [:attrs :name] (str value)))







(fill {:tag :what-ever} "heihei")

(fill {:tag :default} "heihei")

(fill {:tag :input} "haha")

(fill {:span :input} "gege")

(fill {:tag :input
       :attrs {:value "first choice"
               :type "checkbox"}}
      "first choice")








