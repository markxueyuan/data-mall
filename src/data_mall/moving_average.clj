(ns data-mall.moving-average)

;easy way, but inefficient for long windows

(defn average
  [coll]
  (double (/ (reduce + coll) (count coll))))

(defn moving-average
  [window coll]
  (map average (partition window 1 coll)))

;for long windows

(defn partialsums
  [start coll]
  (lazy-seq
   (if-let [lst (seq coll)]
     (cons start (partialsums (+ start (first lst)) (rest lst)))
     (vector start))))


(defn sliding-window-moving-average
  [window coll]
  (map #(/ % window)
       (let [start (reduce + (take window coll))
             diffseq (map - (drop window coll) coll)]
         (partialsums start diffseq))))

;(def integers (iterate #(+ % 2) 0))

;(moving-average 5 (take 100 integers))

;(sliding-window-moving-average 5 integers)





