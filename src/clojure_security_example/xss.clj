(ns clojure-security-example.xss
  (:require [clojure-security-example.helpers :as h]
            [clojure.tools.logging :as log]
            [compojure.core :refer [routes GET POST]]))

(defn xss-attack [text]
  (log/info "attempting xss: " text)
  (h/render "templates/xss.html" {:response text}))

(defn xss-routes [path]
  (routes
    (GET path [] (h/render "templates/xss.html")) 
    (POST path [text] (xss-attack text))))