(ns Miscellaneous.dfl-list)

#_(def url-list
  ["http://movie.douban.com/subject/10833923/comments?sort=time"
   "http://movie.douban.com/subject/10833923/comments?start=51&limit=20&sort=time"
   "http://movie.douban.com/subject/10833923/comments?start=80&limit=20&sort=time"
   "http://movie.douban.com/subject/10833923/comments?start=112&limit=20&sort=time"
   "http://movie.douban.com/subject/10833923/comments?start=163&limit=20&sort=time"
   "http://movie.douban.com/subject/10833923/comments?start=197&limit=20&sort=time"
   "http://movie.douban.com/subject/10833923/comments?start=221&limit=20&sort=time"
   "http://movie.douban.com/subject/10833923/comments?start=253&limit=20&sort=time"
   "http://movie.douban.com/subject/10833923/comments?start=284&limit=20&sort=time"
   "http://movie.douban.com/subject/10833923/comments?start=312&limit=20&sort=time"
   "http://movie.douban.com/subject/10833923/comments?start=335&limit=20&sort=time"
   "http://movie.douban.com/subject/10833923/comments?start=365&limit=20&sort=time"
   "http://movie.douban.com/subject/10833923/comments?start=390&limit=20&sort=time"
   "http://movie.douban.com/subject/10833923/comments?start=419&limit=20&sort=time"
   "http://movie.douban.com/subject/10833923/comments?start=449&limit=20&sort=time"
   "http://movie.douban.com/subject/10833923/comments?start=481&limit=20&sort=time"
   "http://movie.douban.com/subject/10833923/comments?start=518&limit=20&sort=time"
   "http://movie.douban.com/subject/10833923/comments?start=551&limit=20&sort=time"
   ])

(def url-list
  (->> (take 485 (iterate #(+ 20 %) 113))
       ;vec
       (map #(str "http://movie.douban.com/subject/10833923/comments?start=" % "&limit=20&sort=new_score"))))

url-list

;;;;;;;;;;;;;;;;;;;;;;tips

;how to build an arithmetic sequence?


(take 5 (iterate #(+ 20 %) 0))

(vec (take 5 (iterate #(+ 20 %) 0)))





