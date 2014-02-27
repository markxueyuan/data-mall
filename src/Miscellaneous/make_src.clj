(ns Miscellaneous.make-src
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(defn to-srt
  "This turns youtube subtitles form into srt form"
  [file]
  (let [tuples (->> file
                    slurp
                    ((fn [s] (string/split s #"\r\n")))
                    (drop 1)
                    (partition-all 2))
        end-time (->> tuples
                      (drop 1)
                      (map first))
        start-time (->> tuples
                        butlast
                        (map first))
        subtitles (->> tuples
                      butlast
                      (map second))
        timelines (map #(str "00:" %1 ",200" " --> " "00:" %2 ",000") start-time end-time)
        ids (map inc (range (count timelines)))
        func (fn [id timeline subtitle] (str id "\r\n" timeline "\r\n" subtitle "\r\n\r\n"))
        seqs (map func ids timelines subtitles)]
    (apply str seqs)))


(defn print-it
  [writer]
  #(binding [*out* writer]
     (print %)))

(defn file-to
  [file]
  #(with-open [f (io/writer file :append false)]
     ((print-it f) %)))


(defn ->file [address-from address-to]
  ((file-to address-to) (to-src address-from)))

(defn ->file2 [address-from]
  (->file address-from (str address-from ".srt")))

;;;;;;;;;;;;;;;;;;working-area;;;;;;;;;;;;;;;;;;;;;;;;



#_(->file2 "E:/mongoDB_courses/welcome")






