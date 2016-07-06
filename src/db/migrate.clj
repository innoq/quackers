(ns db.migrate
  (:require [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]
            [environ.core :refer [env]]))

(def database-url (env :database-url))

(defn load-config []
      {:migrations (jdbc/load-resources "migrations")
       :datastore (jdbc/sql-database {:connection-uri database-url})}) 

(defn migrate []
      (repl/migrate (load-config)))

(defn rollback []
      (repl/rollback (load-config)))