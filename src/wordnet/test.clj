(ns wordnet.test
  (:require [wordnet.core :as w]
            [wordnet.coerce :as coerce]
            [clojure.string :as s]
            [wordnet.relatedness :as r])
  (:import
    [edu.mit.jwi IDictionary Dictionary RAMDictionary]
    [edu.mit.jwi.item IIndexWord ISynset IWordID IWord Word POS]
    [edu.mit.jwi.data ILoadPolicy]
    [edu.mit.jwi.morph WordnetStemmer]))

;(mg/connect!)

;(mg/set-db! (mg/get-db "wordnet"))

(def wordnet (w/make-dictionary "D:/data/dict/"))

(def wordnet2 (w/make-dictionary "D:/data/dict/" :in-memory))

(def dog (first (wordnet "dog" :noun)))

(:lemma dog)

(:word dog)

(:pos dog)

(:gloss dog)

(map :lemma (w/synonyms dog))

(wordnet "car#n#1")

(wordnet "car")

;(time (r/relatedness dog (first (wordnet "cat" :noun))))
;(time (r/relatedness (first (wordnet "cat" :noun)) dog))

(first (wordnet "man" :noun))
(first (wordnet "woman" :noun))
(time (r/relatedness (first (wordnet "man" :noun)) (first (wordnet "woman" :noun))))
(time (r/relatedness (first (wordnet2 "man" :noun)) (first (wordnet2 "woman" :noun))))

(time (r/relatedness (first (wordnet2 "tower" :noun)) (first (wordnet2 "house" :noun))))

(time (r/relatedness (first (wordnet2 "dog" :noun)) (first (wordnet2 "cat" :noun))))

(time (r/relatedness (first (wordnet2 "beautiful" :adjective)) (first (wordnet2 "pretty" :adjective))))

(time (r/relatedness (first (wordnet2 "China" :noun)) (first (wordnet2 "America" :noun))))
(time (r/relatedness (first (wordnet2 "fight" :verb)) (first (wordnet2 "kill" :verb))))
(time (r/relatedness (first (wordnet2 "push" :verb)) (first (wordnet2 "pull" :verb))))

(time (r/relatedness (first (wordnet2 "heavily" :adverb)) (first (wordnet2 "slowly" :adverb))))

(first (wordnet2 "happily" :adverb))
(wordnet2 "sadly" :adverb)
;;;;;;;;;;;;;;;;;;;;;;;;;try jwi;;;;;;;;;;;;;;;;;;;;

;find original word the passed word stemming from

(.findStems (WordnetStemmer. (doto (Dictionary. (java.io.File. "D:/data/dict/")) .open)) "dogses" (coerce/pos "v"))



;get dog 's multiple meaning
(->> (.getWordIDs (.getIndexWord (doto (Dictionary. (clojure.java.io/file "D:/data/dict/")) .open) (.getLemma (:word dog)) (coerce/pos "n")))
     (map #(.getWord (doto (Dictionary. (java.io.File. "D:/data/dict/")) .open) %))
     identity)

;get dog 's synonyms
(.getWords (.getSynset (:word dog)))


;get dog 's synset's sematically related(hypernym in example) synsets' words
(->> (.getRelatedSynsets (.getSynset (:word dog)) (coerce/pointer :hypernym))
     (map #(.getSynset (doto (Dictionary. (clojure.java.io/file "D:/data/dict/")) .open) %))
     (map #(.getWords %)))

; same as above, just call the related-synsets function
(->> (map #(w/related-synsets % :hypernym) (wordnet "dog" :noun))
     (map vals)
     flatten
     (map :lemma))


;get lexically related words
(map #(w/related-words % :derived-from-adj) (wordnet "beautifully" "r"))

;get sematically related synsets
(map #(w/related-synsets % :hypernym) (wordnet "dog" "n"))

(->> (first (wordnet "dog" "n"))
     :word
     (#(.getSynset %))
     (#(.getRelatedSynsets % (coerce/pointer :hypernym))))


;;;;;;;;;;;;;;;;tips;;;;;;;;;;;

(.indexOf "bdc" "c")
(.indexOf "efg" "efg")
(.indexOf "efg" "fg")
(.indexOf "efg" "eg")

(max-key count "asd" "bsd" "dsd" "long word")

(- 8 2 6)




