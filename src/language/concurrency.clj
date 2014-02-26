(ns language.concurrency)

(defmacro futures
  [n & exprs]
  (vec (for [_ (range n)
             expr exprs]
         `(future ~expr))))


(defmacro wait-futures
  [& args]
  `(doseq [f# (futures ~@args)]
     @f#))


(def sarah (atom {:name "Sarah" :age 25 :wear-glasses? false}))

(swap! sarah update-in [:age] + 3)

(swap! sarah (comp #(update-in % [:age] inc)
                   #(assoc % :wear-glasses? true)))



(def xs (atom #{1 2 3}))

(wait-futures 1 (swap! xs (fn [v]
                            (Thread/sleep 250)
                            (println "trying 4")
                            (conj v 4)))
              (swap! xs (fn [v]
                          (Thread/sleep 500)
                          (println "trying 5")
                          (conj v 5))))


(def x (atom 8000))

(swap! x #(Thread/sleep %))

@xs

(compare-and-set! xs :gaximada "new value")

(compare-and-set! xs @xs "new value")
@xs
(swap! xs (constantly 3))
@xs

(defn echo-watch
  [key identity old new]
  (println key old "变成" new))


(add-watch sarah :wagaga echo-watch)

(swap! sarah update-in [:age] inc)

(add-watch sarah :waga2 echo-watch)

(remove-watch sarah :waga2)

(def history (atom ()))

(defn log->list
  [dest-atom key source old new]
  (when (not= old new)
    (swap! dest-atom conj [new (format "%1$tY-%1$tm-%1$te %1$tH:%1$tM:%1$tS" (java.util.Date.))])))

(add-watch sarah :record (partial log->list history))

@history

