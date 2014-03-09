(ns Miscellaneous.webcrawler
  (:use clojure.java.io
        [clojure.string :only (lower-case)])
  (:require [net.cgrand.enlive-html :as enlive])
  (:import (java.net URL MalformedURLException)
           java.util.concurrent.LinkedBlockingQueue))



(defn links-from
  [base-url html]
  (remove nil? (for [link (enlive/select html [:a])]
                 (when-let [href (-> link :attrs :href)]
                   (try
                     (URL. base-url href)
                     (catch MalformedURLException e))))))



(defn words-from
  [html]
  (let [chunks (-> html
                 (enlive/at [:script] nil)
                 (enlive/select [:body enlive/text-node]))]
    (->> chunks
      (mapcat (partial re-seq #"\w+"))
      (remove (partial re-matches #"\d+"))
      (map lower-case))))

;(words-from (enlive/html-resource (URL. "http://www.bbc.co.uk")))



(def url-queue (LinkedBlockingQueue.))
(def crawled-urls (atom #{}))
(def word-freqs (atom {}))

(declare get-url)

(def agents (set (repeatedly 25 #(agent {::t #'get-url :queue url-queue}))))

(declare run process handle-results)

(defn ^::blocking get-url
  [{:keys [^java.util.concurrent.BlockingQueue queue] :as state}]
  (let [url (as-url (.take queue))]
    (try
      (if (@crawled-urls url)
        state
        (do (Thread/sleep 60000)
        {:url url
         :content (slurp url)
         ::t #'process}))
      (catch Exception e state)
      (finally (run *agent*)))))



(defn process
  [{:keys [url content]}]
  (try
    (let [html (enlive/html-resource (java.io.StringReader. content))]
      {::t #'handle-results
       :url url
       :links (links-from url html)
       :words (reduce (fn [m words]
                        (update-in m [words] (fnil inc 0)))
                      {}
                      (words-from html))})
    (finally (run *agent*))))



(defn ^::blocking handle-results
  [{:keys [url links words]}]
  (try
    (swap! crawled-urls conj url)
    (doseq [url links]
      (.put url-queue url))
    (swap! word-freqs (partial merge-with +) words)
    {::t #'get-url :queue url-queue}
    (finally (run *agent*))))

(defn paused? [agent] (::paused (meta agent)))

(defn run
  ([] (doseq [a agents] (run a)))
  ([a] (when (agents a)
         (send a (fn [{transition ::t :as state}]
                   (when-not (paused? *agent*)
                     (let [dispatch-fn (if (-> transition meta ::blocking)
                                         send-off
                                         send)]
                       (dispatch-fn *agent* transition)))
                   state)))))

(defn pause
  ([] (doseq [a agents] (pause a)))
  ([a] (alter-meta! a assoc ::paused true)))

(defn restart
  ([] (doseq [a agents] (restart a)))
  ([a] (alter-meta! a dissoc ::paused)
       (run a)))

(defn test-crawler
  [agent-count starting-url]
  (def agents (set (repeatedly agent-count
                               #(agent {::t #'get-url :queue url-queue}))))
  (.clear url-queue)
  (swap! crawled-urls empty)
  (swap! word-freqs empty)
  (.add url-queue starting-url)
  (run)
  (Thread/sleep 300000)
  (pause)
  [(count @crawled-urls) (count url-queue)])

;(test-crawler 5 "http://www.baidu.com")



;;;;;;;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;;

;enlive/html-resource can be used either way

#_(enlive/html-resource (URL. "http://www.sohu.com"))
#_(enlive/html-resource (java.io.StringReader. (slurp "http://www.sohu.com")))

;URL. is used like this

#_(URL. (URL. "http://www.sohu.com") "/tianya")

;how to use fnil ?

#_(update-in {} ["hello"] (fnil inc 0))

;how to use merge-with ?

(merge-with + {:a 2 :b 3} {:a 3 :c 4})


