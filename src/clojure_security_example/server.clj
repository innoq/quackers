(ns clojure-security-example.server
  (:require [db.migrate :as db]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojure-security-example.helpers :as h]
            [clojure-security-example.handler :as handler]))

(defn -main [& args]
  (db/migrate)
  (run-jetty
    handler/app
    {:port (h/http-port)
     :join? false
     :ssl? true
     :ssl-port (h/ssl-port)
     :keystore (:keystore env)
     :key-password (:key-password env)
     :host (h/host)}))