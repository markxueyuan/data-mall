(ns data-mall.scrapeTable)

(require '(clojure [string :as string]))
(require '(net.cgrand [enlive-html :as html]))
(require '(incanter [core :as incanter]))
(import [java.net URL])

(defn to-keyword
  "This takes a string and returns a normalized keyword."
  [input]
  (-> input
      string/lower-case
      (string/replace \space \-)
      keyword))



(string/lower-case "SabDe")

(string/replace "mami ai" \space \-)


(to-keyword "A lunar eclipse was considered no less an omen of evil doings")


(defn load-data
  "This loads the data from a table at a URL."
  [url]
  (let [html (html/html-resource (URL. url))
        table (html/select html [:table#data])
        headers (->>
                 (html/select table [:tr :th])
                 (map html/text)
                 (map to-keyword)
                 vec)
        rows (->> (html/select table [:tr])
                  (map #(html/select % [:td]))
                  (map #(map html/text %))
                  (filter seq))]
    (incanter/dataset headers rows)
    ))


(def m (load-data "file:///D:/data/small-sample-table.html"))

(incanter/view m)

(defn general-load-data
  "This provides a versatile scraper for web pages."
  [url table-name]
  (let [html (html/html-resource (URL. url))
        table (html/select html [(keyword table-name)])
        headers (->>
                 (html/select table [:tr :th])
                 (map html/text)
                 (map to-keyword)
                 vec)
        rows (->> (html/select table [:tr])
                  (map #(html/select % [:td]))
                  (map #(map html/text %))
                  (filter seq))]
    (incanter/dataset headers rows)))

(def xy (general-load-data "http://app.finance.ifeng.com/data/mac/spls.php?symbol=05" "table"))

(incanter/view xy)

