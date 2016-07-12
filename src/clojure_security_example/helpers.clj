(ns clojure-security-example.helpers 
  (:require [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ring.util.request :refer [request-url]]
            [selmer.parser :refer [render-file] :as selmer]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [clojure.spec :as s]))

(selmer/add-tag! :antiforgery
                 (fn [_ _]
                   (render-file "templates/anti-forgery.html" {:antiforgery *anti-forgery-token*})))

(defn render 
  ([request filename] (render request filename {}))
  ([request filename params]
   (let [redirect (request-url request)
         p (assoc params :redirect-url redirect)]
     (render-file filename p))))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(def email-spec (s/and string? (s/or :valid? #(re-matches email-regex %) :empty? empty?)))
(def username-spec (s/and string? #(not (empty? %))))
(def password-spec (s/and string? #(not (empty? %))))

(defn http-port []
  (Integer/parseInt (or (env :http-port) "3000")))

(defn ssl-port []
  (Integer/parseInt (or (env :ssl-port) "4000")))

(defn host []
  (or (env :host) "localhost"))

(defn bad-request [] {:status 400 :headers {} :body "Bad Request"})
(defn unauthorized [] {:status 401 :headers {} :body "Unauthorized"})
(defn permission-denied [] {:status 403 :headers {} :body "Permission Denied"})