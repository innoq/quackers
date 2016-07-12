(ns clojure-security-example.authentication
  (:require [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [compojure.core :refer :all]
            [ring.util.response :refer [redirect]]
            [buddy.auth.protocols :as proto]
            [buddy.sign.jws :as jws]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [clojure-security-example.helpers :as h]
            [clojure.spec :as s]
            [clojure-security-example.database :as db]
            [buddy.hashers :as hashers]
            [clojure.tools.logging :as log]
            [clj-time.core :as time]))

(def secret "myveryverysecretsecret")

(s/def ::username h/username-spec)
(s/def ::password h/password-spec)

(s/def :unq/user-login
  (s/keys :req-un [::username ::password]))

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
        (h/permission-denied)
        (h/unauthorized)))))

(defn auth-middleware []
  (let [backend (auth-backend secret  {:alg :hs512})]
    (fn [handler]
      (-> handler
          (wrap-authentication backend)
          (wrap-authorization backend)))))

(defn check-user [username password]
  (when-let [user (first (db/get-user {:username username}))]
    (log/info :user user)
    (when (hashers/check password (:password user)) user)))

(defn do-login [request username password redirect-to]
  (if-let [user (check-user username password)]
    (let [exp (time/plus (time/now) (time/hours 3))
          claims {:user (keyword username)}
                 ;; :exp  exp}
          _      (log/info :claims claims)
          token  (jws/sign claims secret {:alg :hs512 :exp exp})
          session (:session request)
          updated-session (assoc session :jwstoken token)]
      (-> (redirect redirect-to)
          (assoc :session updated-session)))
    (h/bad-request)))
          
(defn auth-routes []
  (routes
    (POST "/login" [username password redirect-to :as request] (do-login request username password redirect-to))))