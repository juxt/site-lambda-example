(ns user
  (:require [clojure.java.io :as io]
            [integrant.core :as ig]
            [integrant.repl :as ig-repl]
            [integrant.repl.state :as ig-state]
            [juxt.site.alpha.main :as site-main]
            [juxt.site.alpha.repl :as site-repl]
            [pro.juxt.config :as config]
            [pro.juxt.site :as site]
            [xtdb.api :as xt]
            [clojure.tools.namespace.repl :refer [disable-reload!]]
            ))

(disable-reload!)
(ig-repl/set-prep! config/prepare-config)

;; Setup a shutdown hook to close cleanly on ctrl+c
(.addShutdownHook (Runtime/getRuntime)
                  (Thread. #(when-not (empty? site-main/system) (ig/halt! site-main/system))))

(def state
  "Convenience access to site-main/system state"
  site-main/system)

(defn is-site-initialized?
  "Check if xtdb contains the schema document for the playground schema."
  []
  (let [db (xt/db (:juxt.site.alpha.db/xt-node state))
        urls (xt/q db '{:find [uri]
                       :where [[uri :juxt.site.alpha/graphql-compiled-schema]]})]
    (urls [(str config/site-endpoint config/target-graphql-schema)])))

(defn ensure-latest-schema
  []
  (site/upload-resource config/target-resources-file)
  (site/upsert-graphql config/target-schema-file))

(defn ensure-init
  "Initialize Site by loading tooling and playground schema
  available in the seed folder. If the schema is already loaded and
  available it assumes Site is already initialized and skip seeding."
  []
  (if (is-site-initialized?)
    (println "### Site already initialised with endpoint"
             (str config/site-endpoint config/target-graphql-schema))
    (do (println "### Site does not contain endpoint"
                 (str config/site-endpoint config/target-graphql-schema) ". Initialising.")
        (let [input config/site-seed-file
              output "target/resources.edn"]
          (with-open [zinput (-> input io/input-stream java.util.zip.ZipInputStream.)]
            (.getNextEntry zinput)
            (io/copy zinput (io/file output)))
          (site-repl/import-resources "target/resources.edn")
          (ensure-latest-schema)))))

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
    (alter-var-root #'site-main/system (constantly ig-state/system))
    (alter-var-root #'state (constantly site-main/system))
    (ensure-init)
    (println "System initialized. Access state from user/state")))

(defn deploy-schema []
  (ensure-latest-schema))

(defn halt []
  (do
   (ig-repl/halt)
   (alter-var-root #'site-main/system (constantly {}))
   (alter-var-root #'state (constantly {}))))

(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)
