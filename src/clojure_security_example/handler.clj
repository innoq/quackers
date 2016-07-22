(ns clojure-security-example.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults secure-site-defaults]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [clojure-security-example.helpers :as h]
            [clojure-security-example.users :refer [user-routes]]
            [clojure-security-example.authentication :refer [auth-routes auth-middleware]]
            [clojure-security-example.quacker :refer [quacker-routes]]
            [clojure.tools.logging :as log]
            [buddy.auth :refer [authenticated?]]))

(defn app-routes []
  (routes
    (auth-routes)
    (quacker-routes)
    (user-routes)
    (route/not-found "Not Found!")))

(defn ignore-trailing-slash
  "Modifies the request uri before calling the handler.
  Removes a single trailing slash from the end of the uri if present.

  Useful for handling optional trailing slashes until Compojure's route matching syntax supports regex.
  Adapted from http://stackoverflow.com/questions/8380468/compojure-regex-for-matching-a-trailing-slash"
  [handler]
  (fn [request]
    (let [uri (:uri request)]
      (handler (assoc request :uri (if (and (not (= "/" uri))
                                            (.endsWith uri "/"))
                                     (subs uri 0 (dec (count uri)))
                                     uri))))))

(defn logging-middleware [handler]
  (fn [request]
    (if (:identity request)
      (log/info :identity (:identity request))
      (log/info :unauthenticated))
    (let [response (handler request)]
      response)))

(defn middleware-settings []
  (-> secure-site-defaults
    ;  (assoc-in [:session :store] (cookie-store {:key "I'm a 16-bit key"}))
      (assoc-in [:security :ssl-redirect] {:ssl-port (h/ssl-port)}))) ;; for non-standard ssl-port

(def app
  (-> (app-routes)
      ignore-trailing-slash
      logging-middleware
      auth-middleware
      (wrap-defaults (middleware-settings))))
