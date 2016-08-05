(ns quackers.quacker
  (:require [quackers.helpers :refer [->int] :as h]
            [quackers.database :as db]
            [clojure.tools.logging :as log]
            [ring.util.response :refer [redirect]]
            [compojure.core :refer [routes GET POST]]
            [buddy.auth.accessrules :refer [restrict]]))


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
