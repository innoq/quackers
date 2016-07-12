(ns clojure-security-example.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults secure-site-defaults]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [clojure-security-example.helpers :as h]
            [clojure-security-example.xss :refer [xss-routes]]
            [clojure-security-example.users :refer [user-routes]]
            [clojure-security-example.authentication :refer [auth-routes]]
            [clojure.tools.logging :as log]))

(defroutes index
  (GET "/" request (h/render request "templates/index.html")))  
               
(defn app-routes []
  (routes
    index
    (auth-routes)
    (xss-routes "/xss")
    (user-routes) ;; creates routes at /users endpoint
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
    (log/info :headers (:headers request))
    (let [response (handler request)]
      response)))

(defn middleware-settings []
  (-> secure-site-defaults
    ;  (assoc-in [:session :store] (cookie-store {:key "I'm a 16-bit key"}))
      (assoc-in [:security :ssl-redirect] {:ssl-port (h/ssl-port)}) ;; for non-standard ssl-port
      (assoc-in [:security :xss-protection :enable?] false))) ;; normally do not change. For this app, we want to show what XSS looks like, so we need to 'enable' it.

(def app
  (-> (app-routes)
      ignore-trailing-slash
     ; logging-middleware
      (wrap-defaults (middleware-settings)))) 
