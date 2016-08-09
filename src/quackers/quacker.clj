(ns quackers.quacker
  (:require [quackers.helpers :refer [->int] :as h]
            [quackers.database :as db]
            [clojure.tools.logging :as log]
            [ring.util.response :refer [redirect]]
            [compojure.core :refer [routes GET POST]]
            [buddy.auth.accessrules :refer [restrict]]))

(defn get-limit [param]
  (try 
    (let [i (->int param)]
      (min i 500))
    (catch Exception e 10)))

(defn get-offset [param]
  (try 
    (->int param)
    (catch Exception e 0)))

(defn index [request]
  (let [limit (get-limit (get-in request [:query-params "limit"]))
        offset (get-offset (get-in request [:query-params "offset"]))
        quacks (db/get-quacks {:limit limit :offset offset})]
    (log/info :limit limit :offset offset)
    (h/render request "templates/index.html" {:quacks quacks
                                              :limit  limit
                                              :offset offset
                                              :back  (- offset limit)
                                              :forward? (>= (count quacks) limit)})))

(defn do-quack [quack identity]
  (when-let [{userid :userid} identity]
    (log/info :userid userid :quack quack :quackity-boo!)
    (db/quack! {:userid userid :quack quack})
    (redirect "/")))

(def quack-auth-rules [{:uri "/"
                        :handler (fn [r] (some? (:identity r)))
                        :request-method :post}])

(defn quacker-routes []
  (routes
    (GET "/" request (index request))
    (POST "/" [quack :as {identity :identity}] (do-quack quack identity))))
