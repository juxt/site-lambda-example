# Terraformed AWS Lambda template with Clojure

This project contains a sample Clojure AWS Lambda application, including an embedded Site/XTDB development environment to test features locally. The Lambda sample application can read/write into Site/XTDB using GraphQL. Terraform scripts will allow to push the Lambda to AWS.

## REPL commands

The template app uses [Integrant](https://github.com/weavejester/integrant) to setup a local [Site](https://github.com/juxt/site) instance. Start the REPL with `clj -M:dev` then type `(go)` to start an embedded Site instance equipped with related tooling.

Now head to: http://localhost:2021/_site/insite/app/apis/graphql?url=/playground/graphql to start playing with a simple "Entity" schema. If requested, use admin/admin user password at login. You should see something like:

![GraphiQL Console](graphiql.png?raw=true "Title")

## Entity Playground Schema

The Entity playground schema is available in `site-modules/playground`. After starting the REPL and `(go)`, the schema is already available as explained above. If you want to play with the schema and change things, there are several ways to keep the schema up to date:

* If you have the REPL open and initialised, you can use `(deploy-schema)` to push the current content of the schema to Site.
* Alternatively, head over to `site-modules/playground` and use the `deploy.sh` script to push the latest changes. The folder also contains an `auto-refresh.sh` script to watch the schema files and update them automatically in case of changed. It requires [entr](https://github.com/clibs/entr).
* The provided [end-2-end test](test/pro/juxt/entity_end2end_test.clj) (see below) also refresh the schema with the latest changes.

## Panic!

Site stores changes in the local XTDB node (the `.xtdb` folder). In case you want to restart from a clean Site installation (which still includes the playground and related tools), use the `(nuke!)` command from the REPL, restart the REPL and hit `(go)` again. It will reset to the original installation and get rid of any subsequent changes. You can also manually `rm -rf .xtdb` with the same effect.

## End2End testing

The project contains an example of [end2end testing](test/pro/juxt/entity_end2end_test.clj) storing, retrieving and delete entities from Site. The test fixtures will start a Site instance unless one is already running (assuming it was started from the REPL).

## The `do` command

The project contains a `do` executable script that invokes the related Clojure and Terraform build commands. It is there to avoid some additional typing and make it quicker to execute commands. Use with:

```clojure
./do test     # Run the test suite
./do compile  # Compile Clojure and create the uberjar ready for deploy
./do init     # One off to setup Terraform
./do plan     # To check Terraform changes
./do apply    # Apply Terraform changes and deploy the Lambda
./do destroy  # To tear down Terraform changes
./do run      # Run the lambda function and print the results
```

Please be aware of the following dependencies when you run the `do` tasks:

* `plan`, `apply` and `destroy` depends on a one off execution of `init`.
* `apply` depends on `compile` to find the `target/lambda.jar` package to deploy.
* `run` depends on `apply` so the Lambda function can be found.

## Local Setup

### AWS credentials for local development

If you have an AWS account, install the https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html[AWS Command Line Interface] also known as the AWS CLI. You should see something like the following after installing:

```bash
λ: aws --version
aws-cli/1.22.87 Python/3.10.4 Linux/5.17.1-arch1-1 botocore/1.24.32
```

Using the account, you should now create an access key and secret which is essentially an id and password pair that an application can use to use AWS services. `aws-cli`, Terraform and any other application wishing to authenticate will need to use this id and password combination. To generate one, follow the steps at this https://docs.aws.amazon.com/cli/latest/userguide/getting-started-prereqs.html#getting-started-prereqs-keys[link]. Once you have the access-key and secret pair, you can use `aws configure` to set it up. This will write a `~/.aws` folder in your home path with the necessary information. The configuration will require a region and an output format. You can find a region suitable to you (for example, `eu-west-1`) by clicking the region selector at the top of the AWS console landing page. The output format can be `json`, `yaml` or plain `text`. You can test that your setup works properly for example listing all the buckets available in S3:

```bash
λ: aws s3 ls
```

If the command shows no output, it's a good sign,  no errors and no buckets to show.

### Installing Terraform

In the next step, we are installing the Terraform command line utilities. There are https://learn.hashicorp.com/tutorials/terraform/install-cli?in=terraform/aws-get-started[good instructions here] if you need help. Once you're done, you should be able to type `terraform` in a terminal and see something like the following:

```bash
λ: terraform -v
Terraform v1.1.7
on linux_amd64
```
