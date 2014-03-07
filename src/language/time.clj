(ns language.time
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as joda]))

(t/date-time 1986 4 2 13 22 4 789)

(t/date-time 1986)

(t/date-time 1986 04 2)

(t/now)

(t/epoch)

(t/hour (t/date-time 1987 9 8 2 2 2))

(t/from-time-zone (t/date-time 1983 5 18) (t/time-zone-for-offset -8))

(t/to-time-zone (t/date-time 1983 5 18) (t/time-zone-for-offset -8))

;看看此时此刻系统记录的北京时间是多少
(t/to-time-zone (t/now) (t/time-zone-for-offset +8))

(t/local-date 2013 3 20)

(t/after? (t/date-time 1986 10) (t/date-time 1986 9))

(t/plus (t/date-time 1986 10 14) (t/months 1) (t/weeks 3))

(t/in-minutes (t/interval (t/date-time 1986 10 2) (t/date-time 1986 10 14)))

(t/in-seconds (t/interval (t/date-time 1986 10 2) (t/date-time 1986 10 14)))

(f/show-formatters)

(joda/from-long 1393372800000)

(joda/to-long "2014-1-1")


(t/in-minutes (t/interval (t/date-time 1986 10 2) (t/date-time 1986 10 14)))



