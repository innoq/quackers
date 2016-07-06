(ns clojure-security-example.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults]]
            [clojure-security-example.helpers :as h]
            [clojure-security-example.xss :refer [xss-routes]]))

(defroutes index
  (GET "/" [] (h/render "templates/index.html")))  
               
(defn app-routes []
  (routes
    index
    (xss-routes "/xss")
    (route/not-found "Not Found!")))

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
  (wrap-defaults (app-routes) middleware-settings)) 
