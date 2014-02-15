(ns lazy-closure)

;when laziness confronts closure
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;dorun can coerce to run all side-effects but discard the head;
(dorun (map identity (range 1e8)))

;however, if we put this in a closure, and let the anonymous fn calls the lazy-seq repeatedly, the closure would have to
;save a larger and larger realized lazy-seq, then the out-of-memory would happen,as follows:

(defn f [g] (g))

(defn t1 [c] (f (fn [] (dorun (map identity c)))))

;(t1 (range 1e8))

;^:once and fn* to solve this problem
;fn* make the anonymous allow a metadata
;^:once makes the anonymous fn calls the lazy-seq only once.

(defn t2 [c] (f (^:once fn* [] (dorun (map identity c)))))

(t2 (range 1e8))

;;for more information,please go:
;;http://blog.fnil.net/index.php/archives/252


;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;

;what is the difference between dorun and doall?

;see the docs of dorun and doall

dorun

doall

