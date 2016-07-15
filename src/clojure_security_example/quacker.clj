(ns clojure-security-example.quacker
  (:require [clojure-security-example.helpers :as h]
            [clojure-security-example.database :as db]
            [clojure.tools.logging :as log]
            [compojure.core :refer [routes GET POST]]))

(defn ->int [s] (Integer/parseInt s))

(defn index [request]
  (log/info :request request)
  (let [limit (->int (get-in request [:query-params "limit"] "20"))
        offset  (->int (get-in request [:query-params "offset"] "0"))
        quacks (db/get-quacks {:limit limit :offset offset})]
    (log/info quacks)
    (h/render request "templates/index.html" {:quacks quacks
                                              :limit  limit
                                              :offset offset})))
                                              
(defn quacker-routes []
  (routes 
    (GET "/" request (index request))))