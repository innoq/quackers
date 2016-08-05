(ns quackers.helpers
  (:require [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ring.util.request :refer [request-url]]
            [selmer.parser :refer [render-file] :as selmer]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [clojure.string :refer [starts-with?]]
            [ring.util.response :refer [redirect]]))

(defn ->int [s] (Integer/parseInt s))

(selmer/add-tag! :antiforgery
                 (fn [_ _]
                   (render-file "templates/anti-forgery.html" {:antiforgery *anti-forgery-token*})))

(defn render
  ([request filename] (render request filename {}))
  ([request filename params]
   (let [redirect (get params :redirect (request-url request))
         p (assoc params :redirect-url redirect
                         :auth (:identity request))]
     (render-file filename p))))

(def email-regex #"^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")

(defn http-port []
  (->int (or (env :http-port) "3000")))

(defn ssl-port []
  (->int (or (env :ssl-port) "4000")))

(defn host []
  (or (env :host) "localhost"))

(defn site-url []
  (str "https://" (host) ":" (ssl-port)))

(defn get-redirect [url]
  (let [index-page (site-url)]
    (if url
      (if (starts-with? url index-page) url index-page)
      index-page)))

(defn redirect-to-login [redirect-url]
  (redirect (str "/login?redirect-to=" (get-redirect redirect-url))))

(defn auth-error-handler [request _v]
  (redirect-to-login (request-url request)))

(defn bad-request [] {:status 400 :headers {} :body "Bad Request"})
(defn unauthorized [] {:status 401 :headers {} :body "Unauthorized"})
(defn permission-denied [] {:status 403 :headers {} :body "Permission Denied"})
