(ns clojure-security-example.authentication
  (:require [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [compojure.core :refer :all]
            [ring.util.response :refer [redirect]]
            [ring.util.request :refer [request-url]]
            [buddy.auth.protocols :as proto]
            [buddy.sign.jwt :as jwt]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [clojure-security-example.helpers :as h]
            [clojure.spec :as s]
            [clojure-security-example.database :as db]
            [clojure.string :refer [starts-with?]]
            [buddy.hashers :as hashers]
            [clojure.tools.logging :as log]
            [buddy.auth.accessrules :refer [success error wrap-access-rules]]
            [clojure-security-example.users :refer [user-auth-rules]]
            [clj-time.core :as time]))

(def secret "myveryverysecretsecret")

(defn auth-backend [secret]
  (reify
    proto/IAuthentication
    (-parse [_ request]
      (let [token (get-in request [:session :jwttoken])]
        token))
    (-authenticate [_ _request data]
      (try
        (jwt/unsign data secret)
        (catch clojure.lang.ExceptionInfo e nil)))

    proto/IAuthorization
    (-handle-unauthorized [_ request _metadata]
      (if (authenticated? request)
        (h/permission-denied)
        (h/unauthorized)))))

(defn get-redirect [url]
  (if url
    (if (starts-with? url (h/site-url)) url (h/site-url))
    (h/site-url)))

(defn redirect-to-login [redirect-url]
  (redirect (str "/login?redirect-to=" (get-redirect redirect-url))))

(defn on-error [request v]
  (log/info :request (request-url request) :v v)
  (redirect-to-login (request-url request)))

(defn auth-middleware [handler]
  (let [backend (auth-backend secret)]
      (-> handler
          (wrap-access-rules {:rules user-auth-rules :on-error on-error})
          (wrap-authentication backend)
          (wrap-authorization backend))))

(defn check-user [username password]
  (when (and (string? username) (seq username))
    (when-let [user (first (db/get-user {:username username}))]
      (log/info :user user)
      (when (hashers/check password (:password user)) user))))

(defn login-form [request redirect-to]
  (let [valid-redirect (get-redirect redirect-to)]
    (h/render request "templates/login.html" {:redirect valid-redirect})))

(defn do-login [request username password redirect-to]
  (if-let [user (check-user username password)]
    (let [exp (time/plus (time/now) (time/hours 3))
          claims {:user username
                  :userid (:userid user)
                  :exp  exp}
          token  (jwt/sign claims secret)
          session (:session request)
          updated-session (assoc session :jwttoken token)]
      (-> (redirect (get-redirect redirect-to))
          (assoc :session updated-session)))
    (redirect redirect-to)))

(defn do-logout [{session :session} redirect-to]
  (-> (redirect (get-redirect redirect-to))
      (assoc :session (dissoc session :jwttoken))))

(defn auth-routes []
  (routes
    (GET  "/login"  [redirect-to :as request] (login-form request redirect-to))
    (POST "/login"  [username password redirect-to :as request] (do-login request username password redirect-to))
    (GET  "/logout" [redirect-to :as request] (do-logout request redirect-to))))
