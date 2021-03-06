(ns quackers.users
  (:require [compojure.core :refer :all]
            [quackers.helpers :refer [->int] :as h]
            [clojure.tools.logging :as log]
            [quackers.database :as db]
            [ring.util.response :refer [redirect]]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [buddy.auth :refer [authenticated?]]
            [buddy.hashers :as hashers]))

(def route-map
  (let [basepath "/users"
        show     (str basepath "/:username")]
    {:index basepath
     :new   (str basepath "/new")
     :show  show
     :edit  (str show "/edit")}))

(defn create-user! [usermap]
  (let [password (:password usermap)
        digest   (hashers/encrypt password)
        newmap   (assoc usermap :password digest)]
    (db/add-user! newmap)
    newmap))

(defn update-password! [usermap]
  (let [password (:password usermap)
        digest   (hashers/encrypt password)
        newmap   (assoc usermap :password digest)]
    (db/update-user-password!)
    newmap))

(defn update-password! [usermap]
  (when-let [password (:password usermap)]
    (let [digest   (hashers/encrypt password)
          newmap   (assoc usermap :password digest)]
      (db/update-user-password! newmap)
      newmap)))

(defn valid-username? [username]
  (b/valid? {:username username} :username [v/required v/string]))

(defn validate-user-create [usermap]
  (b/validate usermap 
    :username [v/required v/string]
    :email    [v/string [v/matches h/email-regex :message "invalid email"]]
    :password [v/required v/string]))

(defn validate-user-update [usermap]
  (b/validate usermap
    :username [v/required v/string]
    :email    [v/string [v/matches h/email-regex :message "invalid email"]]
    :password v/string))

(defn create-form [request user]
  (h/render request "templates/users/create.html" user))

(defn create-user [request user]
  (log/info :create (dissoc user :password))
  (let [[errors validated] (validate-user-create user)]
    (if errors
      (create-form request (assoc user :errors errors))
      (if (first (db/get-user user))
        (create-form request (assoc user :errors {:username ["already exists"]}))
        (let [new-user (create-user! user)]
          (redirect (:index route-map)))))))

(defn index [request]
  (let [users (db/get-users)]
    (h/render request "templates/users/index.html" {:users users})))

(defn show [request username]
  (log/info :show username)
  (when (valid-username? username)
    (let [user (first (db/get-user {:username username}))]
      (when user
        (let [limit   (->int (get-in request [:query-params "limit"] "10"))
              offset  (->int (get-in request [:query-params "offset"] "0"))
              quacks  (db/get-quacks-for-user {:limit limit :offset offset
                                               :username username})]
            (h/render request "templates/users/show.html"
                      (assoc user :quacks quacks :limit limit :offset offset
                                  :back (- offset limit) :forward? (>= (count quacks) limit))))))))

(defn delete [username]
  (log/info :delete username)
  (when (valid-username? username)
    (let [_ (db/delete-user! {:username username})]
      (redirect (:index route-map)))))

(defn edit-form [request user]
  (h/render request "templates/users/edit.html" user))

(defn edit [request username]
  (log/info :edit username)
  (when (valid-username? username)
    (let [user (first (db/get-user {:username username}))]
      (if user (edit-form request (dissoc user :password))))))

(defn update [request user]
  (log/info :update (dissoc user :password))
  (let [to-validate (if (empty? (:password user)) (dissoc user :password) user)]
    (let [[errors validated] (validate-user-update to-validate)]
      (if errors
        (edit-form request (assoc to-validate :errors errors))
        (let [_ (update-password! to-validate)
              _ (db/update-user-email! to-validate)]
             (redirect (:index route-map)))))))

(defn is-user? [request]
   (when-let [{user :user} (:identity request)]
     (= user (get-in request [:match-params :username]))))

(def user-auth-rules [{:uri (:index route-map)
                       :handler authenticated?
                       :request-method :get}
                      {:uri (:edit route-map)
                       :handler is-user?}
                      {:uri (:show route-map)
                       :handler is-user?
                       :request-method #{:put :delete :post}}])

(defn user-routes []
  (routes
    (GET    (:index route-map) request (index request))
    (GET    (:new route-map)   request (create-form request {}))
    (POST   (:index route-map) {params :params :as request} (create-user request params))
    (GET    (:show route-map)  [username :as request] (show request username))
    (GET    (:edit route-map)  [username :as request] (edit request username))
    (PUT    (:show route-map)  [username email password :as request] (update request {:username username :email email :password password}))
    (DELETE (:show route-map)  [username] (delete username))))
