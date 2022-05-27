(ns pro.juxt.lambda
  (:require [clojure.java.io :as io])
  (:gen-class
   :name pro.juxt.LambdaFn
   :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler]))

(defn -handleRequest
  [this input-stream output-stream context]
  (let [logger (.getLogger context)]
    (with-open [r (io/reader input-stream)
                w (io/writer output-stream :encoding "US-ASCII")]
      (let [input (slurp r)]
        (.log logger (str "Input message: " input))
        (.log logger (str "Input message type: " (class input)))
        (.write w input)))))
