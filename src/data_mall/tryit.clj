(ns data-mall.tryit)

(def money (atom nil))

(defn waiter
  []
  (future
    (loop []
      (if (nil? @money)
        (recur)
        (println "wine")))))


(defn pay
  []
  (reset! money :x))

(waiter)


(pay)
