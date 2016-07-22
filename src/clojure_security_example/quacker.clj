(ns clojure-security-example.quacker
  (:require [clojure-security-example.helpers :as h]
            [clojure-security-example.database :as db]
            [clojure.tools.logging :as log]
            [ring.util.response :refer [redirect]]
            [compojure.core :refer [routes GET POST]]
            [buddy.auth.accessrules :refer [restrict]]))

(defn ->int [s] (Integer/parseInt s))

(defn index [request]
  (let [limit (->int (get-in request [:query-params "limit"] "10"))
        offset  (->int (get-in request [:query-params "offset"] "0"))
        quacks (db/get-quacks {:limit limit :offset offset})]
    (h/render request "templates/index.html" {:quacks quacks
                                              :limit  limit
                                              :offset offset
                                              :back  (- offset limit)
                                              :forward? (>= (count quacks) limit)})))

(defn do-quack [quack identity]
  ;; TODO Validate
  (when-let [{userid :userid} identity]
    (log/info :userid userid :quack quack :quackity-boo!)
    (db/quack! {:userid userid :quack quack}))
  (redirect "/"))

(defn quacker-routes []
  (routes
    (GET "/" request (index request))
    (POST "/" [quack :as {identity :identity}]
        (restrict (do-quack quack identity) {:handler (fn [r] (some? (:identity r)))
                                             :on-error h/auth-error-handler}))))
