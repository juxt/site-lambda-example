(ns pro.juxt.test-fixtures
  (:require [clojure.test :refer :all]
            [user :refer [go]]
            [pro.juxt.site :as site]
            [pro.juxt.config :refer [site-port]])
  (:import [java.net Socket]))

(defn delete-all-entities
  []
  (doseq [{:keys [id]} (site/entities)]
    (site/delete-entity id)))

(defn ensure-site-running
  "Don't attempt to start the system if already running.
  Start the system otherwise.
  This allows tests to run with an already running site instance.
  If there is no running site instance, starts a new one with current
  configuration and loaded schemas.
  In either case, the running site instance requires installation "
  []
  (when (try
         (Socket. "localhost" site-port)
         false
         (catch Exception e true))
    (go)))

(defn site-setup [tests]
  (ensure-site-running)
  (tests)
  (delete-all-entities))
