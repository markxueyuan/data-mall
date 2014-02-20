(ns data-mall.parser
  (:use [protoflex.parse :as parse]))

;;;;;;;;;;;;;;;;;;;;data;;;;;;;;;;

(def test-data ">gi|5524211|gb|AAD44166.1| cytochrome b [Elephas maximus maximus]
LCLYTHIGRNIYYGSYLYSETWNTGIMLLLITMATAFMGYVLPWGQMSFWGATVITNLFSAIPY
IGTNLVEWIWGGFSVDKATLNRFFAFHFILPFTMVALAGVHLTFLHETGSNNPLGLTSDSDKIP
FHPYYTIKDFLGLLILILLLLLLALLSPDMLGDPDNHMPADPLNTPLHIKPEWYFLFAYAILRS
VPNKLGGVLALFLSIVILGLMPFLHTSKHRSMMLRPLSQALFWTLTMDLLTLTWIGSQPVEYPY
TIIGQMASILYFSIILAFLPIAGXIENY")

(def test-data2 ">I don't think there are ghosts in this planet,
however, I do believe there are uncountable amounts of ghosts out of earth and all around the universe. ")

(def s1 "1abc,def,ghi\n2jkl,mno,pqr\n")

(def s2 "'1a,bc',def,ghi\n2jkl,mno,pqr\n")

(def s3 "1abc\tdef\tghi\n2jkl\tmno\tpqr\n")

(def s4 "'1a\tbc'\tdef\tghi\n2jkl\tmno\tpqr\n")


;;;;;;;;;;;;fn;;;;;;;;;;;;;;;;;;

;parse fasta

(defn <|
  [l r]
  (let [l-output (l)]
    (r) l-output))

(defn nl
  []
  (chr-in #{\newline \return}))

(defn defline
  []
  (chr \>) (<| #(read-to-re #"[\n\r]+") nl))

(defn acid-code
  []
  (chr-in #{\A \B \C \D \E \F \G \H \I \J \K \L \M \N \O \P \Q \R \S \T \U \V \W \X \Y \Z \- \*}))

(defn acid-code-line
  []
  (<| #(multi+ acid-code) #(parse/attempt nl)))

(defn fasta
  []
  (parse/ws?)
  (let [dl (defline)
        gls (apply str (flatten (parse/multi+ acid-code-line)))]
    {:defline dl :gene-seq gls}))

(defn parse-fasta
  [input]
  (parse/parse fasta input :eof false :auto-trim false))



;parse csv

(declare detect-sep csv-1)

(defn csv
  ([] (csv (no-trim #(detect-sep))))
  ([sep] (multi* (fn [] (no-trim #(csv-1 sep))))))

(defn csv-1
  [sep]
  (sep-by #(any-string sep) #(chr sep)))

(defn detect-sep
  []
  (let [m (mark-pos)
        s (attempt #(any dq-str sq-str))
        s (if s s (no-trim #(read-to-re #",|\t")))
        sep (read-ch)]
    (back-to-mark m)
    sep))

;(parse csv s3)
;(parse csv s4)







;;;;;;;;;;;tips;;;;;;;;;;;;;;;;


;flatten takes any nested combination of sequential things (lists, vectors, etc.)
;and returns their contents as a single, flat sequence.

(flatten [1 2 3 4 [5 6 [7 8]]])
