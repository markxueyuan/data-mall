(ns language.colAbstr)

(ns language.hello)

(defn scaffold
  [interface]
  (doseq [[iface methods] (->> interface
                               .getMethods
                               (map #(vector (.getName (.getDeclaringClass %))
                                             (symbol (.getName %))
                                             (count (.getParameterTypes %))))
                               (group-by first))]
    (println (str " " iface))
    (doseq [[_ name argcount] methods]
      (println (str " "
                    (list name (into '[this] (take argcount (repeatedly gensym)))))))))

(map #(.getName %) (.getMethods clojure.lang.IPersistentSet))

(map #(.getDeclaringClass %) (.getMethods clojure.lang.IPersistentSet))
(map #(.getName (.getDeclaringClass %)) (.getMethods clojure.lang.IPersistentSet))

(map #(.getParameterTypes %) (.getMethods clojure.lang.IPersistentSet))
