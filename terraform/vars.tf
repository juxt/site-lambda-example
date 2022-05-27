variable "lambda_payload_filename" {
  default = "../target/lambda.jar"
}

variable "lambda_function_handler" {
  default = "pro.juxt.LambdaFn"
}

variable "lambda_runtime" {
  default = "java11"
}

variable "lambda_memory" {
  default = "512"
}

variable "region" {
  default = "eu-west-1"
}

variable "lambda_timeout" {
  default = 900
}
