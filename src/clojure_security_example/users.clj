(ns clojure-security-example.users
  (:require [compojure.core :refer :all]
            [environ.core :refer [env]]
            [clojure-security-example.helpers :as h]
            [clojure.tools.logging :as log]
            [yesql.core :refer [defqueries]]
            [ring.util.response :refer [redirect]]
            [clojure.spec :as s]
            [buddy.hashers :as hashers]))

(def route-map
  (let [basepath "/users"
        show     (str basepath "/:username")]
    {:index basepath
     :new   (str basepath "/new")
     :show  show
     :edit  (str show "/edit")})) 

(def db-spec {:connection-uri (env :database-url)})

;; Imports the functions get-users, get-user, add-user!, update-user-email!, update-user-password!, delete-user!
(defqueries "db/users.sql" {:connection db-spec})

(defn create-user! [usermap]
  (let [password (:password usermap)
        digest   (hashers/encrypt password)
        newmap   (assoc usermap :password digest)]
    (add-user! newmap)
    newmap))

(defn update-password! [usermap]
  (when-let [password (:password usermap)]
    (let [digest   (hashers/encrypt password)
          newmap   (assoc usermap :password digest)]
      (update-user-password! newmap)
      newmap)))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email (s/and string? (s/or :valid? #(re-matches email-regex %) :empty? empty?)))
(s/def ::username (s/and string? #(not (empty? %))))
(s/def ::password  (s/and string? #(not (empty? %))))

(s/def :unq/user-create
  (s/keys :req-un [::username ::password]
          :opt-un [::email]))

(s/def :unq/user-update
  (s/keys :req-un [::username]
          :opt-un [::email ::password]))

(defn missing-key? [usermap keyword]
  (let [value (get usermap keyword)]
    (or (empty? value) (nil? value))))
  
(defn add-information [usermap validator required-keys]
  (let [data (s/explain-data validator usermap)
        _    (log/info :validation data)
        invalid (mapcat :in (second (first data)) data)
        missing (filter (partial missing-key? usermap) required-keys)
        all  (distinct (concat invalid missing))
        invalid-map (into {} (map (fn [k] [(keyword (str (name k) "-invalid")) true]) all))
        result (assoc (merge usermap invalid-map) :errors true)]
    (log/info :result result)
    result))
    
(defn create-form [user]
  (h/render "templates/users/create.html" user))

(defn create-user [user]
  (log/info :create (dissoc user :password))
  (let [valid-user (s/conform :unq/user-create user)]
    (if (= valid-user :clojure.spec/invalid) 
      (create-form (add-information user :unq/user-create [:username :password]))
      (if (first (get-user user))
        (create-form (assoc user :errors true :already-exists true))
        (let [new-user (create-user! user)]
           (redirect (:index route-map)))))))
      
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

(defn delete [username]
  (log/info :delete username)
  ;; TODO validate
  (let [_ (delete-user! {:username username})]
    (redirect (:index route-map))))

(defn edit-form [user]
  (h/render "templates/users/edit.html" user))

(defn edit [username]
  (log/info :edit username)
  (let [user (first (get-user {:username username}))]
    ;; TODO 404
    (if user (edit-form (dissoc user :password)))))  

(defn update [user]
  (log/info :update (dissoc user :password))
  (let [to-validate (if (empty? (:password user)) (dissoc user :password) user)
        valid-user (s/conform :unq/user-update to-validate)]
    (if (= valid-user :clojure.spec/invalid)
        (edit-form (add-information to-validate :unq/user-update []))
        (let [_ (update-password! to-validate)
              _ (update-user-email! to-validate)]
             (redirect (:index route-map))))))          

(defn user-routes []
  (routes
    (GET    (:index route-map) [] (list))
    (GET    (:new route-map)   [] (create-form {}))
    (POST   (:index route-map) {params :params} (create-user params))
    (GET    (:show route-map)  [username] (show username))
    (GET    (:edit route-map)  [username] (edit username))
    (PUT    (:show route-map)  [username email password] (update {:username username :email email :password password}))
    (DELETE (:show route-map)  [username] (delete username))))