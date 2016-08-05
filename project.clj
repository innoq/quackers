(defproject quackers "0.1.0-SNAPSHOT"
  :description "Examples of common security attacks (e.g. XSS)"
  :url "http://gitlab.innoq.com/innoq/clojure-security-example"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0-alpha9"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [selmer "1.0.7"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ragtime "0.6.1"]
                 [environ "1.0.3"]
                 [yesql "0.5.3"]
                 [buddy "1.0.0"]
                 [com.h2database/h2 "1.4.192"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.danielsz/system "0.3.0"]]
  :plugins [[lein-environ "1.0.3"]]
  :main quackers.core
  :aliases {"migrate" ["run" "-m" "db.migrate/migrate"]
            "rollback" ["run" "-m" "db.migrate/rollback"]})
