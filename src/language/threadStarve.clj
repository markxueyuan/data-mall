(ns language.threadStarve
  (:import (java.lang Thread)))

(def counter (ref 0))

(def a1 (agent :a1))

(def a2 (agent :a2))

(defn start-agents [msg a1-sleep a2-sleep]
  (send a1 msg a1-sleep) (send a2 msg a2-sleep))

(defn debug
  [msg]
  (print (str msg \n)) (.flush *out*))

(defn debug!
  [msg]
  (io! (print (str msg \n)) (.flush *out*)))

(defn starve-out [tag sleep-for]
  (let [retries (atom 0)]
    (dosync
     (let [c @counter]
       (when-not (zero? @retries)
         (debug (str ":starveout " tag
                     " :retry " @retries
                     " :counter " c)))
       (swap! retries inc)
       (Thread/sleep sleep-for)
       (ref-set counter (inc c))
       (send *agent* starve-out sleep-for)
       tag))))

(defn starve-safe [tag sleep-for]
  (let [retries (atom 0)]

    (dosync (let [c @counter]
              (swap! retries inc)
              (Thread/sleep sleep-for)
              (ref-set counter (inc c))))
    (when-not (zero? @retries)
      (debug! (str ":safe-starve " tag
                   " :retry " @retries
                   " :counter " @counter)))
    (send *agent* starve-safe sleep-for)
    tag))

;(start-agents starve-out 50 1000)

(start-agents starve-safe 50 1000)

;(shutdown-agents)
;;;;;;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn uuu [x] (+ x 1))
uuu
(str uuu)
