(ns pro.juxt.entity-end2end-test
  (:require [clojure.test :refer :all]
            [pro.juxt.site :as site]
            [pro.juxt.test-fixtures :as test-fixtures]))

(use-fixtures :once test-fixtures/site-setup)

(deftest entity-CRUD-test
  (testing "should retrieve the created entities"
    (is (= [{:name "name0" :img "img0"}
            {:name "name1" :img "img1"}
            {:name "name2" :img "img2"}]
           (->> (range 3)
                (map #(-> [(str "name" %) (str "img" %)]))
                site/create-entities
                (map #(dissoc % :id)))))))
