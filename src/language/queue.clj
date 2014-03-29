(ns language.queue)

;One big problem when dealing with very large datasets cocurrently is coordinating and managing
;the flow of data between different parts of program. If one part produces data too quickly, or
;another part processes it too slowly, depending on how you look at it, the message queue between
;the two can get backed up. If that happens, memory will get filled up with the messages and data
;waiting to be processed. The solution for this in clojure is: use seque. This uses an instance
;of java.util.concurrent.LinkedBlockingQueue to pull values from a lazy sequence. It works ahead
;of where we're pulling values out of the queue, but not too far ahead. And once we've wrapped a
;sequence with seque, we can treat it just like ay other sequence.

(take 20 (seque 5 (range Integer/MAX_VALUE)))


