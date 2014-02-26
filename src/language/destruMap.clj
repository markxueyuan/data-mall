(ns language.destruMap)

(def m {:a 5 :b 6
        :c [7 8 9]
        :d {:e 10 :f 11}
        "foo" 88
        42 false})

(let [{a :a b :b} m]
  (+ a b))

(let [{f "foo"} m]
  (+ f 12))

(let [{v 42} m]
  (if v 1 0))

(let [{x 3 y 8} [1 2 3 4 5 6 7 8 9]]
  (+ x y))

(let [{{e :e} :d} m]
  (* e 3))

(let [{{x 0 y 2} :c} m]
  (+ x y))

(let [{[a _ c] :c} m]
  (+ a c))

(def map-in-vector ["Jue" {:birthday (java.util.Date. 87 11 6) :city "wuxi"}])

(let [[a {b :birthday}] map-in-vector]
  (str a " is born on " b))

(let [{x :a y :b :as all} {:a 2 :b 3 :c 4 :d 5}]
      (assoc all :part-sum (+ x y)))


(let [{u :u a :a :or {u 50}} m]
  (+ u a))

(defn make-user
  [& [user-id]]
  {:user-id (or user-id
                (str (java.util.UUID/randomUUID)))})

(make-user)

(make-user "abc")

(defn make-user2
  [& user-id]
  {:user-id (or user-id
                (str (java.util.UUID/randomUUID)))})

(make-user2 "abc")



