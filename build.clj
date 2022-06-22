(ns build
  (:refer-clojure :exclude [compile apply])
  (:require [org.corfield.build :as b]
            [clojure.java.shell :refer [sh]]))

(defn test "Run the tests." [opts]
  (b/run-tests opts))

(defn compile
  "Compile Clojure and create the jar package"
  [opts]
  (let [cmd ["clj" "-M:uberdeps" "--target" "target/lambda.jar"
             "--main-class" "pro.juxt.lambda"]
        output (clojure.core/apply sh (into cmd opts))]
    (println (:out output))
    (println (:err output))))

(defn- terraform [cmd & opts]
  (let [cmd (clojure.core/apply sh "terraform" "-chdir=terraform" cmd opts)]
    (println (:out cmd))
    (println (:err cmd))))

(defn init [opts] (terraform "init"))
(defn plan [opts] (terraform "plan"))
(defn apply [opts] (terraform "apply" "-auto-approve"))
(defn destroy [opts] (terraform "destroy" "-auto-approve"))

(defn run [opts]
  (let [cmd ["aws" "lambda" "invoke"
             "--invocation-type" "RequestResponse"
             "--function-name" "lambda"
             "--region" "eu-west-1"
             "--log-type" "Tail"
             "--payload" "{\"some\":\"input\"}"
             "target/results.txt"]
        output (clojure.core/apply sh (into cmd opts))]
    (println (:out output))
    (println (:err output))
    (println (slurp "target/results.txt"))))
