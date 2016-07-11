(ns clojure-security-example.server
  (:require [db.migrate :as db]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojure-security-example.handler :as handler]))

(defn- port []
  (Integer/parseInt (or (env :port) "3000")))

(defn host []
  (or (env :host) (str "http://localhost" (port))))

(defn -main [& args]
  (db/migrate)
  (run-jetty
    handler/app
    {:port (port)}))