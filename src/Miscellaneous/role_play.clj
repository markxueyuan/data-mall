(ns Miscellaneous.role-play)

(defmacro futures
  [n & exprs]
  (vec (for [_ (range n)
             expr exprs]
         `(future ~expr))))


(defmacro wait-futures
  [& args]
  `(doseq [f# (futures ~@args)]
     @f#))

(defn character
  [name & {:as opts}]
  (ref (merge {:name name :items #{} :health 500}
              opts)))

(def smaug (character "Smaug" :health 500 :strength 400 :items (set (range 50))))

(def bilbo (character "Bilbo" :health 100 :strength 100))

(def gandalf (character "Gandalf" :health 75 :mana 750))

(defn loot
  [from to]
  (dosync
   (when-let [item (first (:items @from))]
     (alter to update-in [:items] conj item)
     (alter from update-in [:items] disj item))))

(loot smaug bilbo)

(wait-futures 1 (while (loot smaug gandalf))
              (while (loot smaug bilbo)))

@bilbo
@gandalf
@smaug

(map (comp count :items deref) [bilbo gandalf])

(filter (:items @gandalf) (:items @bilbo))
