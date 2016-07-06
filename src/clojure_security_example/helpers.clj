(ns clojure-security-example.helpers 
  (:require [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [selmer.parser :refer [render-file]]))

(defn render 
  ([filename] (render filename {}))
  ([filename params]
   (let [p (assoc params :antiforgery *anti-forgery-token*)]
     (render-file filename p))))