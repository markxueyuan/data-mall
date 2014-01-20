(ns Miscellaneous.monitor)

(let [log-capacity 5000
      events (agent [])]
  (defn log-event [e]
    (send events #(if (== log-capacity (count %))
                    (-> % (conj e) (subvec 1))
                    (conj % e)))
    e)
  (defn events [] @events))




(doseq [request (repeatedly 10000 (partial rand-nth [{:referrer "twitter.com"}
                                                    {:referrer "facebook.com"}
                                                    {:referrer "twitter.com"}
                                                    {:referrer "reddit.com"}]))]
  (log-event request))

(count (events))

(frequencies (events))











