# Site House Schema

In order to be able to run `./deploy.sh` you'll need the following:

* Install [Babashka](https://babashka.org/) a self-contained Clojure scripting tool
* Make sure `site-modules/site` command is executable with `chmod 755 site-modules/site`

## Schema auto-refresh (optional)

Install the [entr](https://github.com/eradman/entr) command line runner. Then run `cd site-modules/house; ./auto-refresh.sh` to watch the schema and resources files and deploy them when they change.
