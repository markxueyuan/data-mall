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

(def gandalf (character "Gandalf" :health 75 :mana 7500))

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

(defn attack
  [aggressor target]
  (dosync
   (let [damage (* (rand 0.1) (:strength @aggressor))]
     (commute target update-in [:health] #(max 0 (- % damage)))
     )))

(defn heal
  [healer target]
  (dosync
   (let [aid (* (rand 0.1) (:mana @healer))]
     (when (pos? aid)
       (commute healer update-in [:mana] - (max 5 (/ aid 5)))
       (commute target update-in [:health] + aid)))))

(def alive? (comp pos? :health))

(defn play
  [character action other]
  (while (and (alive? @character)
              (alive? @other)
              (action character other))
    (Thread/sleep (rand-int 50))))

(wait-futures 1
              (play bilbo attack smaug)
              (play smaug attack bilbo))

(dosync
 (alter smaug assoc :health 500)
 (alter bilbo assoc :health 100))

(wait-futures 1
              (play bilbo attack smaug)
              (play smaug attack bilbo)
              (play gandalf heal bilbo))

@bilbo

(map (comp #(select-keys % [:name :health :mana]) deref) [smaug bilbo gandalf])


(defn- enforce-max-health
  [{:keys [name health]}]
  (fn [character-data]
    (or (<= (:health character-data) health)
        (throw (IllegalStateException. (str name " is already at max health!"))))))

(defn character
  [name & {:as opts}]
  (let [cdata (merge {:name name :items #{} :health 500} opts)
        cdata (assoc cdata :max-health (:health cdata))
        validators (list* (enforce-max-health {:name name :health (:health cdata)})
                          (:validators cdata))]
    (ref (dissoc cdata :validators)
         :validator #(every? (fn [v] (v %)) validators))))

(def bilbo (character "Bilbo" :health 100 :strength 100))

@bilbo

(heal gandalf bilbo)



















