(defproject data-mall "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"local" ~(str (.toURI (java.io.File. "maven_repository")))}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [incanter/incanter-core "1.5.4"]
                 [incanter/incanter-io "1.5.4"]
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
                 [clj-time "0.6.0"]]

  :repl-options {:nrepl-middleware [lighttable.nrepl.handler/lighttable-ops]}
 ; :resource-paths ["src/main/resource"]

  )









