(ns clojure-security-example.database
  (:require [environ.core :refer [env]]
            [yesql.core :refer [defqueries]]))

;; Imports the functions get-users, get-user, add-user!,
;;     update-user-email!, update-user-password!, delete-user!
(defqueries "db/users.sql" {:connection {:connection-uri (env :database-url)}})