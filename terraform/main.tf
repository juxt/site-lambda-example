provider "aws" {
  region = "${var.region}"
}

resource "aws_iam_role" "iam_clj_lambda" {
  name = "iam_for_lambda"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "basic-exec-role" {
  role       = "${aws_iam_role.iam_clj_lambda.name}"
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_lambda_function" "test-clj-lambda" {
  function_name = "lambda"
  filename = "${var.lambda_payload_filename}"

  source_code_hash = "${filebase64sha256(var.lambda_payload_filename)}"
  timeout = "${var.lambda_timeout}"
  role = "${aws_iam_role.iam_clj_lambda.arn}"
  handler = "${var.lambda_function_handler}"
  runtime = "${var.lambda_runtime}"
  memory_size = "${var.lambda_memory}"
}
