(ns clojure-security-example.database
  (:require [environ.core :refer [env]]
            [yesql.core :refer [defqueries]]))

(def db-conn {:connection {:connection-uri (env :database-url)}})

;; Imports the functions get-users, get-user, add-user!,
;;     update-user-email!, update-user-password!, delete-user!
(defqueries "db/users.sql" db-conn)

(defqueries "db/quacks.sql" db-conn)