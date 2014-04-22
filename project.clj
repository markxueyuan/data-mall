(defproject data-mall "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"local" ~(str (.toURI (java.io.File. "maven_repository")))
                 "mvn-repo" "http://ansjsun.github.io/mvn-repo/"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 ;[jfree/jcommon "1.0.16"]
                 ;[jfree/jfreechart "1.0.13"]
                 [incanter "1.5.4"]
                 [org.clojure/data.json "0.2.3"]
                 [org.clojure/java.jdbc "0.3.0"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [mysql/mysql-connector-java "5.1.25"]
                 [org.clojure/data.csv "0.1.2"]
                 [enlive "1.1.5"]
                 [lein-light-nrepl "0.0.10"]
                 [clj-diff "1.0.0-SNAPSHOT"]
                 [stanford/classifier "3.3.1"]
                 [stanford/tmt "0.4.0"]
                 [clj-time "0.6.0"]
                 [org.ansj/ansj_seg "0.9"]
                 [protoflex/parse-ez "0.4.2"]
                 [valip "0.2.0"]
                 ;[simhash "0.1.0-SNAPSHOT"]
                 [com.novemberain/monger "1.7.0"]
                 [clj-excel "0.0.1"]
                 [com.novemberain/validateur "2.0.0-beta3"]
                 [org.clojure/tools.trace "0.7.8"]
                 [org.clojure/tools.logging "0.2.6"]
                 ;[edu.ucdenver.ccp/kr-sesame-core "1.4.8"]
                 [org.slf4j/slf4j-simple "1.7.6"]
                 [criterium "0.4.3"]
                 [nz.ac.waikato.cms.weka/weka-dev "3.7.10"]
                 [calx "0.2.1"]
                 ]

  :repl-options {:nrepl-middleware [lighttable.nrepl.handler/lighttable-ops]}
  :jvm-opts ["-Xmx1g"]
 ; :resource-paths ["src/main/resource"]

  )









