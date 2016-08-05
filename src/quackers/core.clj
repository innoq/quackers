(ns quackers.core
  (:require
    [system.repl :refer [set-init! start go stop reset]]
    [clojure.tools.namespace.repl :refer [refresh]]
    [db.migrate :as db]
    [quackers.systems :refer [dev-system]]))

(set-init! #'dev-system)

(defn -main
  "starts a dev system"
  [& args]
  (db/migrate)
  (start))
