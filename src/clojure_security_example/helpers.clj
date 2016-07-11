(ns clojure-security-example.helpers 
  (:require [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ring.util.request :refer [request-url]]
            [selmer.parser :refer [render-file]]
            [clojure.tools.logging :as log]
            [clojure.spec :as s]))

(defn render 
  ([request filename] (render request filename {}))
  ([request filename params]
   (let [redirect (request-url request)
         p (assoc params :antiforgery *anti-forgery-token*
                         :redirect-url redirect)]
     (log/info :uri redirect)
     (render-file filename p))))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(def email-spec (s/and string? (s/or :valid? #(re-matches email-regex %) :empty? empty?)))
(def username-spec (s/and string? #(not (empty? %))))
(def password-spec (s/and string? #(not (empty? %))))