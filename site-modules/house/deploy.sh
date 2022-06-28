SITE_BASE_URI='http://localhost:2021'
GRAPHQL_PATH='/house/graphql'
GRAPHQL_API_URL="${SITE_BASE_URI}${GRAPHQL_PATH}"
SITE_COMMAND='../site'
echo Deploying schema to $GRAPHQL_PATH
$SITE_COMMAND -s check-token || $SITE_COMMAND get-token -u admin -p admin
$SITE_COMMAND post-resources --file resources.edn
$SITE_COMMAND put-graphql --file schema.graphql --path $GRAPHQL_PATH -b $SITE_BASE_URI
