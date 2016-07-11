(ns clojure-security-example.xss
  (:require [clojure-security-example.helpers :as h]
            [clojure.tools.logging :as log]
            [compojure.core :refer [routes GET POST]]))

(defn xss-attack [request text]
  (log/info "attempting xss: " text)
  (h/render request "templates/xss.html" {:response text}))

(defn xss-routes [path]
  (routes
    (GET path request (h/render request "templates/xss.html")) 
    (POST path [text :as request] (xss-attack request text))))