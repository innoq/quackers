(ns clojure-security-example.quacker
  (:require [clojure-security-example.helpers :as h]
            [clojure-security-example.database :as db]
            [clojure.tools.logging :as log]
            [ring.util.response :refer [redirect]]
            [compojure.core :refer [routes GET POST]]))

(defn ->int [s] (Integer/parseInt s))

(defn index [request]
  (let [limit (->int (get-in request [:query-params "limit"] "10"))
        offset  (->int (get-in request [:query-params "offset"] "0"))
        quacks (db/get-quacks {:limit limit :offset offset})]
    (h/render request "templates/index.html" {:quacks quacks
                                              :limit  limit
                                              :offset offset})))
                                              
(defn do-quack [userid quack]
  (log/info :userid userid :quack quack)
  ;; TODO Validate
  (db/quack! {:userid (->int userid) :quack quack})
  (redirect "/"))

(defn quacker-routes []
  (routes 
    (GET "/" request (index request))
    (POST "/" [userid quack] (do-quack userid quack)))) ;; TODO AUTHORIZE!