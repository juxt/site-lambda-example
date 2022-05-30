(ns pro.juxt.config
  (:require [xtdb.rocksdb :refer [->kv-store]]
            [juxt.site.alpha :as-alias site]
            [clojure.java.io :as io]
            [aero.core :as aero]
            [integrant.core :as ig]))

(defmethod aero/reader 'ig/ref [_ _ value] (ig/ref value))

(let [lock (Object.)]
  (defn- ig-load-ns
    "Concurrent load namespaces that loads all integrant configs."
    [system-config]
    (locking lock
      (ig/load-namespaces system-config))))

(def aero-config
  (let [env (keyword (or (System/getenv "APP_ENV") (System/getProperty "app.env") "dev"))]
    (aero/read-config (io/file "config.edn") {:profile env})))

(defn prepare-config
  "Prepare Aero environment and load Integrant defmethods"
  []
  (let [cfg (:ig/system aero-config)]
    (ig-load-ns cfg)
    cfg))

(def site-seed-file (::site/seed-file aero-config))
(def graphql-schema (::site/graphql-schema aero-config))
(def site-endpoint (::site/base-uri aero-config))
(def site-user (::site/basic-auth-user aero-config))
(def site-pwd (::site/basic-auth-pwd aero-config))
(def xt-node (get-in aero-config [:ig/system :juxt.site.alpha.db/xt-node]))
(def site-port (get-in aero-config [:juxt.site.alpha.server/server ::site/port]))
