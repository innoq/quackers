(ns quackers.authentication
  (:require [buddy.auth.middleware :refer [wrap-authentication]]
            [compojure.core :refer :all]
            [ring.util.response :refer [redirect]]
            [buddy.auth.protocols :as proto]
            [buddy.sign.jwt :as jwt]
            [buddy.auth.backends :as backends]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [quackers.helpers :as h]
            [quackers.database :as db]
            [buddy.hashers :as hashers]
            [clojure.tools.logging :as log]
            [buddy.auth.accessrules :refer [success error wrap-access-rules]]
            [quackers.users :refer [user-auth-rules]]
            [quackers.quacker :refer [quack-auth-rules]]
            [clj-time.core :as time]))

(def secret "myveryverysecretsecret")

(defn set-auth-header [handler]
  (fn [request]
    (let [token (get-in request [:session :jwttoken])
          token-string (str "Token " token)
          new-request (assoc-in request [:headers "authorization"] token-string)]
      (handler new-request))))

(defn auth-middleware [handler]
  (let [backend (backends/jws {:secret secret})]
      (-> handler
          (wrap-access-rules {:rules (concat quack-auth-rules user-auth-rules) :on-error h/auth-error-handler})
          (wrap-authentication backend)
          set-auth-header)))

(defn check-user [username password]
  (when (and (string? username) (seq username))
    (when-let [user (first (db/get-user {:username username}))]
      (log/info :user user)
      (when (hashers/check password (:password user)) user))))

(defn login-form [request redirect-to]
  (let [valid-redirect (h/get-redirect redirect-to)]
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
      (-> (redirect (h/get-redirect redirect-to))
          (assoc :session updated-session)))
    (h/redirect-to-login redirect-to)))

(defn do-logout [{session :session} redirect-to]
  (-> (redirect (h/get-redirect redirect-to))
      (assoc :session (dissoc session :jwttoken))))

(defn auth-routes []
  (routes
    (GET  "/login"  [redirect-to :as request] (login-form request redirect-to))
    (POST "/login"  [username password redirect-to :as request] (do-login request username password redirect-to))
    (GET  "/logout" [redirect-to :as request] (do-logout request redirect-to))))
