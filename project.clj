(defproject clojure-security-example "0.1.0-SNAPSHOT"
  :description "Examples of common security attacks (e.g. XSS)"
  :url "http://gitlab.innoq.com/innoq/clojure-security-example"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [selmer "1.0.7"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ragtime "0.6.1"]
                 [environ "1.0.2"]
                 [com.h2database/h2 "1.4.192"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-environ "1.0.2"]]
  :ring {:handler clojure-security-example.handler/app}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.3.0"]] 
                   :env {:database-url "jdbc:h2:./db/app"}}}
  :aliases {"migrate" ["run" "-m" "db.migrate/migrate"]
            "rollback" ["run" "-m" "db.migrate/rollback"]})
       
