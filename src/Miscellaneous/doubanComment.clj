(ns Miscellaneous.doubanComment)

(require '(clojure [string :as string]))
(require '(net.cgrand [enlive-html :as html]))
(require '(incanter [core :as incanter]))
(import [java.net URL])
(require '(data-mall [connectDB3 :as db]))
(require '[clojure.java.jdbc :as jdbc])
(require '(Miscellaneous [dfl-list :as dfl]))

(defn dry
  [input]
  (if (= input nil)
    input
    (apply string/trim input))
  )

(defn trimer
  [input]
  (if (= input nil)
    input
    (string/trim input)))


(defn commentor
  [comment-item]
  (let [people (first (html/select comment-item [:span.comment-info :a]))
        {address :href} (:attrs people)
        name (apply str (:content people))]
    {:name name
     :address address}))

(defn rating&time
  [comment-item]
  (let [rate (html/select comment-item [:span.comment-info :span])
        score (:title (apply :attrs rate))
        time  (dry (:content (second rate)))]
    {:score score
     :time time}
  ))

(defn text
  [comment-item]
  (let [t (->> (html/select comment-item [:div.comment :p])
               (apply :content)
               first
               trimer
               )]
    {:text t}))

(defn vote
  [comment-item]
  (let [v (->> (html/select comment-item [:div.comment :span.comment-vote :span])
               (apply :content)
               (apply str)
               )]
    {:vote v}))

(defn merge-fields
  [c-i]
  (conj (commentor c-i)
        (rating&time c-i)
        (text c-i)
        (vote c-i)))

(defn load-data
  [html-url]
  (let [html (html/html-resource (URL. html-url))
        items (html/select html [:div.comment-item])]
    (incanter/to-dataset (map merge-fields items))))



(def xy (load-data "http://movie.douban.com/subject/10833923/comments?start=51&limit=20&sort=time"))

(incanter/view xy)




;;;;;;;;;;;;;;;;;;;;;;;;write data into database;;;;;;;;;;;;;;;

(def db-spec
  (db/mysql-connector "doubancomment"))

(def db-52
  (db/connect52 "xueyuan"))

(def ddl (str "id INT NOT NULL AUTO_INCREMENT, "
              "vote TEXT NULL, "
              "text LONGTEXT NULL, "
              "time TEXT NULL, "
              "score TEXT NULL, "
              "name TEXT NULL, "
              "address TEXT NULL, "
              "PRIMARY KEY (id), "
              "UNIQUE INDEX id_UNIQUE (id ASC)"))

(defonce table (db/create-new-table db-spec "dflnew" ddl))

(doseq [entry dfl/url-list]
  (->> entry
       load-data
       (db/write-into-table db-spec "dflnew"))
       (Thread/sleep 5000)
       )

(defonce table52 (db/create-new-table db-52 "dfl" ddl))


(doseq [entry dfl/url-list]
  (->> entry
       load-data
       (db/write-into-table db-52 "dfl")
       ))




;;;;;;;;;;;tips;;;;;;;;;;;

(string/trim "\n  2012-3-4\n")



;;;;;;;;;;;;not that smart;;;;;;;;;;;;;;;;

(def aa (html/html-resource (URL. "http://movie.douban.com/subject/10833923/comments?start=1&limit=20&sort=new_score")))

aa

(def bb
(html/select aa [:div.comment-item :span.comment-info :a]))

bb

(first bb)


(html/select aa [:div.comment-item])
(def hh (html/select aa [:div.comment-item]))

(vec hh)

(def dd (first (html/select aa [:div.comment-item])))

(html/select dd [:span.comment-info :a])

(def cc (first (html/select aa [:div.comment-item])))

(commentor dd)

(html/select dd [:span.comment-info :span])

(def ee (html/select dd [:span.comment-info :span]))

(second ee)

(string/trim (first (:time (rating&time dd))))

(rating&time dd)

(html/select dd [:div.comment :p])

(def ff (html/select dd [:div.comment :p]))

(first (apply :content ff))




(text dd)


(html/select dd [:div.comment :span.comment-vote :span])

(merge-rows dd)


(->> (html/select dd [:div.comment :p])
               (apply :content))





