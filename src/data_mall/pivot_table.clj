(ns data-mall.pivot-table)

(defn extract
  [ks seq]
  (for [k ks]
    (map #(get % k) seq)
    ))

(defn process
  [ks fns seq]
  (let [k-fn (zipmap ks fns)
        k-seq (zipmap ks seq)]
    (for [k ks]
      (let [my-fn (k k-fn)
            my-seq (k k-seq)]
        [k (my-fn my-seq)]))))

(defn stats
  [pivots ks fns entry]
  (let [result (->> entry
                    second
                    seq
                    (extract ks)
                    (process ks fns))
        piv (->> entry
                 first
                 (zipmap pivots))]
    (into piv result)))


(defn pivot-table
  [pivots ks fns resultsets]
  (let [rels (into pivots ks)
        rows (map #(map % rels) resultsets)
        maps (map #(zipmap rels %) rows)
        groups (group-by (apply juxt pivots) maps)]
    (for [entry groups]
      (stats pivots ks fns entry))))



(defn ->num [i]
  (if (integer? i)
    (double i)
    (Double. i)))

(defn remove-null
  [col]
  (let [flt #(or (= "" %)(nil? %))]
    (remove flt col)))

(defn list-it [col] (map identity col))

(defn sum
  [col]
  (->> col
       remove-null
       (map ->num)
       (apply +)))


(defn average
  [col]
  (let [nums (count (remove-null col))
        sums (sum col)]
    (/ sums nums)))

