(ns clojure-security-example.authentication
  (:require [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [compojure.core :refer :all]
            [ring.util.response :refer [redirect]]
            [buddy.auth.protocols :as proto]
            [buddy.sign.jws :as jws]
            [buddy.auth :refer [authenticated? throw-unauthorized]]))
  

(def secret "myveryverysecretsecret")

(defn auth-backend [secret algorithm]
  (reify
    proto/IAuthentication
    (-parse [_ request]
      (let [token (get-in request [:session :jwstoken])]
        token))
    (-authenticate [_ _request data]
      (try
        (jws/unsign data secret algorithm)
        (catch clojure.lang.ExceptionInfo e nil)))

    proto/IAuthorization
    (-handle-unauthorized [_ request _metadata]
      (if (authenticated? request)
        {:status 403 :headers {} :body "Permission denied"}
        {:status 401 :headers {} :body "Unauthorized"}))))

(defn auth-middleware []
  (let [backend (auth-backend secret  {:alg :hs512})]
    (fn [handler]
      (-> handler
          (wrap-authentication backend)
          (wrap-authorization backend)))))
          
(defn auth-routes []
  (routes
    (GET "/login" [] (redirect "/"))))