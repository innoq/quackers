(ns clojure-security-example.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults]]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [clojure.tools.logging :as log]
            [selmer.parser :refer [render-file]]))

(defn render 
  ([filename] (render filename {}))
  ([filename params]
   (let [p (assoc params :antiforgery *anti-forgery-token*)]
     (render-file filename p))))

(defn xss-attack [text]
  (log/info "params" text)
  (log/info (render "templates/xss.html" {:response text}))
  (render "templates/xss.html" {:response text}))

(defroutes app-routes
  (GET "/" [] (render "templates/index.html"))
  (GET "/xss" [] (render "templates/xss.html")) 
  (POST "/xss" [text] (xss-attack text))
  (route/not-found "Not Found!"))

(def middleware-settings
  {:params    {:urlencoded true
               :multipart  true
               :nested     true
               :keywordize true}
   :cookies   true
   :session   {:flash true
               :cookie-attrs {:http-only true}}
   :security  {:anti-forgery   true
               :xss-protection {:enable? false, :mode :block} ;; in site-defaults this is true.
               :frame-options  :sameorigin
               :content-type-options :nosniff}
   :static    {:resources "public"}
   :responses {:not-modified-responses true
               :absolute-redirects     true
               :content-types          true
               :default-charset        "utf-8"}})

(def app
  (wrap-defaults app-routes middleware-settings)) 
