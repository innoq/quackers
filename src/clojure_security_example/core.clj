(ns clojure-security-example.core
  (:require
    [system.repl :refer [set-init! start go stop reset]]
    [clojure.tools.namespace.repl :refer [refresh]]
    [db.migrate :as db]
    [clojure-security-example.systems :refer [dev-system]]))

(set-init! #'dev-system)

(defn -main
  "starts a dev system"
  [& args]
  (db/migrate)
  (start))
