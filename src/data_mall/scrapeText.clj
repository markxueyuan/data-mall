(ns data-mall.scrapeText)

(require '(clojure [string :as string]))
(require '(net.cgrand [enlive-html :as html]))
(require '(incanter [core :as incanter]))
(import [java.net URL])

(defn get-family
  "This takes an article element and returns the family name."
  ([article]
   (string/join
    (map html/text (html/select article [:header :h2])))
   ))

(defn get-person
  "This takes a list item and returns a map of the person's name and relationship."
  ([li]
   (let [[{pnames :content} rel] (:content li)]
     {:name (apply str pnames)
      :relationship (string/trim rel)})
   ))

(defn get-rows
  "This takes an article and returns the person mappings, with the family name added."
  ([article]
   (let [family (get-family article)]
     (map #(assoc % :family family)
          (map get-person
               (html/select article [:ul :li]))))
   ))

(defn load-data
  "This downloads the HTML page and pulls the data out of it."
  [html-url]
  (let [html (html/html-resource (URL. html-url))
        articles (html/select html [:article])]
    (incanter/to-dataset (mapcat get-rows articles))
    ))


(defonce data-sample (load-data (str "http://www.ericrochester.com/"
                                     "clj-data-analysis/data/small-sample-list.html")))

(incanter/view data-sample)





;;;;;;;not that smart

(def aa (html/html-resource (URL. (str "http://www.ericrochester.com/"
                                     "clj-data-analysis/data/small-sample-list.html"))))

(def bb
  (->> (html/select aa [:article :li])
     first
     :content
     ;first
     )
  )

(let [[{pnames :content} relation] bb]
  relation)



(->> (html/select aa [:article :li])
     first
     :content
     ;first
     )

(apply str (let [[{a :tag c :content} b] bb]
   c))

(def cc (let [[{a :tag c :content} b] bb]
   b))

(string/trim cc)


(def dd (html/html-resource (URL. "http://movie.douban.com/subject/10833923/comments?sort=new_score")))

dd






