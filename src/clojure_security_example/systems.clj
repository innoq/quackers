(ns clojure-security-example.systems
  (:require [system.core :refer [defsystem]]
            [clojure-security-example.helpers :as h]
            (system.components
              [jetty :refer [new-web-server]])
            [environ.core :refer [env]]
            [clojure-security-example.handler :as handler]))

(def options
  {:port (h/http-port)
   :join? false
   :ssl? true
   :ssl-port (h/ssl-port)
   :keystore (:keystore env)
   :key-password (:key-password env)
   :host (h/host)})

(defsystem dev-system
  [:web (new-web-server (h/http-port) handler/app options)])
