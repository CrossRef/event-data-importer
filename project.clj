(defproject event-data-importer "0.1.0"
  :description "Import data into Event Data."
  :url "http://eventdata.crossref.org/"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [event-data-common "0.1.60"]
                 [org.clojure/data.json "0.2.6"]
                 [yogthos/config "0.8"]
                 [com.auth0/java-jwt "2.2.1"]
                 [clj-time "0.12.2"]
                 [org.clojure/tools.logging "0.3.1"]]
  :main ^:skip-aot event-data-importer.core
  :jvm-opts ["-Duser.timezone=UTC"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
