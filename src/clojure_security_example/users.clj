(ns clojure-security-example.users
  (:require [compojure.core :refer :all]
            [environ.core :refer [env]]
            [clojure-security-example.helpers :as h]
            [clojure.tools.logging :as log]
            [yesql.core :refer [defqueries]]
            [ring.util.response :refer [redirect]]
            [clojure.spec :as s]
            [buddy.hashers :as hashers]))

(def db-spec {:connection-uri (env :database-url)})

;; Imports the functions get-users, get-user, add-user!, update-user!, delete-user!
(defqueries "db/users.sql" {:connection db-spec})

(defn create-user! [usermap]
  (let [password (:password usermap)
        digest   (hashers/encrypt password)
        newmap   (assoc usermap :password digest)]
    (add-user! newmap)
    newmap))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email (s/and string? (s/or :valid? #(re-matches email-regex %) :empty? empty?)))
(s/def ::username (s/and string? #(not (empty? %)) #(not (first (get-user {:username %})))))
(s/def ::password  (s/and string? #(not (empty? %))))

(s/def :unq/user-create
  (s/keys :req-un [::username ::password]
          :opt-un [::email]))

(defn contains-key? [usermap keyword]
  (let [value (get usermap keyword)]
    (or (empty? value) (nil? value))))
  

(defn add-information [usermap]
  (let [data (s/explain-data :unq/user-create usermap)
        _    (log/info :validation data)
        invalid (mapcat :in (second (first data)) data)
        missing (filter (partial contains-key? usermap) [:username :password])
        all  (distinct (concat invalid missing))
        _    (log/info :all)
        invalid-map (into {} (map (fn [k] [(keyword (str (name k) "-invalid")) true]) all))
        result (assoc (merge usermap invalid-map) :errors true)]
    (log/info :result result)
    result))
    

(defn create-form [user]
  (h/render "templates/users/create.html" user))

(defn create-user [user basepath]
  (log/info :create (dissoc user :password))
  (let [valid-user (s/conform :unq/user-create user)]
    (if (= valid-user :clojure.spec/invalid)
      (create-form (add-information user))
      (let [new-user (create-user! user)]
           (redirect basepath)))))
      
(defn list []
  (let [users (get-users)]
    (h/render "templates/users/index.html" {:users users})))

(defn show [username]
  (log/info :show username)
  ;; TODO validate
  (let [user (first (get-user {:username username}))]
    (log/info user)
    ;; TODO 404
    (if user (h/render "templates/users/show.html" user))))

(defn delete [username basepath]
  (log/info :delete username)
  ;; TODO validate
  (let [_ (delete-user! {:username username})]
    (redirect basepath)))

(defn edit-form [user]
  (h/render "templates/users/edit.html" user))

(defn edit [username]
  (log/info :edit username)
  (let [user (first (get-user {:username username}))]
    ;; TODO 404
    (if user (edit-form (dissoc user :password)))))  

(defn update [user basepath]
  (log/info :update user)
  ;; TODO!
  (redirect basepath))

(defn user-routes [path]
  (routes
    (GET path [] (list))
    (GET (str path "/new") [] (create-form {}))
    (POST path {params :params} (create-user params path))
    (GET (str path "/:username") [username] (show username))
    (GET (str path "/:username/edit") [username] (edit username))
    (PUT (str path "/:username") [username email password] (update {:username username :email email :password password} path))
    (DELETE (str path "/:username") [username] (delete username path))))