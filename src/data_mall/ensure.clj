(ns data-mall.ensure
  (require [clojure.string :as s])
  (import [java.io File]))

(def input-files
  (filter #(.isFile %) (file-seq (File. "E:/data/brown"))))

(def finished (ref false))

(def total-docs (ref 0))

(def total-words (ref 0))

(def freqs (ref {}))

(def running-report (agent {:term nil :frequency 0 :ratio 0.0}))


(defn tokenize-brown
  [input-str]
  (->> (s/split input-str #"\s+")
       (map #(first (s/split % #"/" 2)))
       (filter #(> (count %) 0))
       (map s/lower-case)
       (map keyword)
       ))

#_(tokenize-brown "This/is really/a fucking/world !")

(defn accum-freq
  [m token]
  (assoc m token (inc (m token 0))))

(defn compute-file
  [files]
  (dosync
   (if-let [[s & ss] (seq files)]
     (let [tokens (tokenize-brown (slurp s))
           tc (count tokens)
           fq (reduce accum-freq {} tokens)]
       (commute total-docs inc)
       (commute total-words #(+ tc %))
       (commute freqs #(merge-with + % fq))
       (send-off *agent* compute-file)
       ss)
     (do (alter finished (constantly true)) '()))))

(defn compute-report
  [{term :term :as report}]
  (dosync
   (when-not @finished
     (send *agent* compute-report))
   (let [term-freq (term (ensure freqs) 0);?
         tw (ensure total-words)]
     (assoc report
       :frequency term-freq
       :ratio (if (zero? tw)
                nil
                (float (/ term-freq tw)))))))

(defn compute-frequencies
  [inputs term]
  (let [a (agent inputs)]
    (send running-report #(assoc % :term term))
    (send running-report compute-report)
    (send-off a compute-file)))

(compute-frequencies input-files :party)

[@finished @running-report]



;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;

(s/split "a2b3c4d5e6" #"\d+")

(s/split "a2b3c4d5e6" #"\d+" 3)

(merge-with + {:a 2 :b 3} {:a 4 :b 5 :c 6} {:b 7 :c 8 :d 9})

;constantly accepts any inputs

(def boring (constantly ["hehe" "haha"]))

(boring [1 2 3 4])

(boring 1 2 3 4)

(boring "1234")



