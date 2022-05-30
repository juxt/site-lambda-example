(ns user
  (:require [clojure.java.io :as io]
            [integrant.core :as ig]
            [integrant.repl :as ig-repl]
            [integrant.repl.state :as ig-state]
            [juxt.site.alpha.main :as site]
            [juxt.site.alpha.repl :as site-repl]
            [pro.juxt.config :as config]
            [xtdb.api :as xt]))

(ig-repl/set-prep! config/prepare-config)

;; Setup a shutdown hook to close cleanly on ctrl+c
(.addShutdownHook (Runtime/getRuntime)
                  (Thread. #(when-not (empty? site/system) (ig/halt! site/system))))

(def state
  "Convenience access to site/system state"
  site/system)

(defn is-site-initialized?
  "Check if xtdb contains the schema document for the playground schema."
  []
  (let [db (xt/db (:juxt.site.alpha.db/xt-node state))
        urls (xt/q db '{:find [uri]
                       :where [[uri :juxt.site.alpha/graphql-compiled-schema]]})]
    (urls [(str config/site-endpoint config/graphql-schema)])))

(defn ensure-init
  "Initialize Site by loading tooling and playground schema
  available in the seed folder. If the schema is already loaded and
  available it assumes Site is already initialized and skip seeding."
  []
  (when-not is-site-initialized?
    (let [input config/site-seed-file
          output "target/resources.edn"]
      (with-open [zinput (-> input io/input-stream java.util.zip.ZipInputStream.)]
        (.getNextEntry zinput)
        (io/copy zinput (io/file output)))
      (site-repl/import-resources "target/resources.edn"))))

(defn nuke!
  "Panic button. Warning: this throws away the current Site state
  stored in the local .xtdb folder and restart from scratch (all
  local changes will be lost, but Site will still have login, insite
  graphiQL and sample entities. Usage:
  1. (nuke!)
  2. restart REPL
  3. (go)"
  ([] (nuke! (io/file ".xtdb")))
  ([file]
   (run! nuke! (.listFiles file))
   (io/delete-file file)))

(defn go
  "First, it verifies Site has been initialized"
  []
  (let [system (ig-repl/go)]
    (alter-var-root #'site/system (constantly ig-state/system))
    (alter-var-root #'state (constantly site/system))
    (ensure-init)
    (println "System initialized. Access state from user/state")))

(defn halt []
  (do
   (ig-repl/halt)
   (alter-var-root #'site/system (constantly {}))
   (alter-var-root #'state (constantly {}))))

(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)
