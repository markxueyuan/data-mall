(ns Miscellaneous.datastructure)

(deftype Node [car cdr])

(def node (Node. "foo" nil))

(.car node)
(.cdr node)

(new Node "bar" nil)


(def linked-list (Node. "foo" (Node. "bar" (Node. "baz" nil))))


(.car (.cdr linked-list))

(deftype Node [^:volatile-mutable car ^:volatile-mutable cdr])

;well this makes it impossible to access the field from the outside world.

(definterface INode
  (getCar [])
  (getCdr [])
  (setCar [x])
  (setCdr [x]))

(deftype Node [^:volatile-mutable car ^:volatile-mutable cdr]
  INode
  (getCar [this] car)
  (getCdr [this] cdr)
  (setCar [this x] (set! car x) this)
  (setCdr [this x] (set! cdr x) this))

(def node (Node. "foo" nil))

(.getCar node)

(.getCdr (.setCdr node "bar"))

(definterface INode
  (getCar [])
  (setCar [x])
  (getCdr [])
  (setCdr [n])
  (reverse []))

(deftype Node
  [^:volatile-mutable car ^:volatile-mutable ^INode cdr]
  INode
  (getCar [_] car)
  (setCar [_ x] (set! car x))
  (getCdr [_] cdr)
  (setCdr [_ n] (set! cdr n))
  (reverse [this]
            (loop [cur this
                   new-head nil]
              (if-not cur
                new-head
                (recur (.getCdr cur) (Node. (.getCar cur) new-head))
                ))))

(def linked-list (Node. "c" (Node. "b" (Node. "a" nil))))

(linked-list )



(.. linked-list getCar)

(.. linked-list reverse getCar)


(deftype Node
  [^:volatile-mutable car ^:volatile-mutable ^INode]
  )





