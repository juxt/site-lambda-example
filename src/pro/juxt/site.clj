(ns pro.juxt.site
  (:require [pro.juxt.config :as config]
            [clj-http.client :as http]
            [jsonista.core :as j]
            [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as cske]))

(def mapper
  (j/object-mapper
   {:decode-key-fn csk/->kebab-case-keyword}))

(def query-add-entity
"
mutation addEntity($entity: EntityInput) {
  addEntity(entity: $entity) {
    id
    img
    name
  }
}
")

(def query-entity-by-id
"
query Entity($id: ID!) {
  entity(id: $id) {
    id
    img
    name
  }
}
")

(def query-delete-entity
"
mutation DeleteEntity($id: ID!) {
  deleteEntity(id: $id) {
    id
  }
}
")

(def query-entities
"
query AllEntities {
  entities {
    id
    img
    name
  }
}
")

(defn get-site-token
  "Retrieve the site token necessary to authorize
  all other interactions."
  []
  (let [endpoint (str config/site-endpoint "/_site/token")
        response (http/post
                  endpoint
                  {:headers {"content-type" "application/x-www-form-urlencoded"}
                   :basic-auth [config/site-user config/site-pwd]
                   :body "grant_type=client_credentials"})
        body (j/read-value (:body response))
        access-token (get body "access_token")]
    access-token))

(defn create-entity [entity-name entity-img]
  (let [endpoint (str config/site-endpoint "/playground/graphql")
        body {:query query-add-entity
              :variables (cske/transform-keys
                          csk/->camelCaseKeyword
                          {:entity {:name entity-name
                                    :img entity-img}})}
        response (http/post
                  endpoint
                  {:body (j/write-value-as-string body)
                   :headers {"authorization" (str "Bearer " (get-site-token))
                             "Content-Type" "application/json"}})]
    (-> response
        :body
        (j/read-value mapper)
        :data
        :add-entity)))

(defn create-entities [entities]
  (doall (map (fn [[name img]] (create-entity name img)) entities)))

(defn entity [id]
  (let [endpoint (str config/site-endpoint "/playground/graphql")
        body {:query query-entity-by-id
              :variables (cske/transform-keys csk/->camelCaseKeyword {:id id})}
        response (http/post
                  endpoint
                  {:body (j/write-value-as-string body)
                   :headers {"authorization" (str "Bearer " (get-site-token))
                             "Content-Type" "application/json"}})]
    (-> response
        :body
        (j/read-value mapper)
        :data
        :entity)))

(defn delete-entity [id]
  (let [endpoint (str config/site-endpoint "/playground/graphql")
        body {:query query-delete-entity
              :variables (cske/transform-keys csk/->camelCaseKeyword {:id id})}
        response (http/post
                  endpoint
                  {:body (j/write-value-as-string body)
                   :headers {"authorization" (str "Bearer " (get-site-token))
                             "Content-Type" "application/json"}})]
    (-> response
        :body
        (j/read-value mapper)
        :data
        :delete-entity
        :id)))

(defn entities []
  (let [endpoint (str config/site-endpoint "/playground/graphql")
        body {:query query-entities
              :variables {}}
        response (http/post
                  endpoint
                  {:body (j/write-value-as-string body)
                   :headers {"authorization" (str "Bearer " (get-site-token))
                             "Content-Type" "application/json"}})]
    (-> response
        :body
        (j/read-value mapper)
        :data
        :entities)))
