SITE_BASE_URI='http://localhost:2021'
GRAPHQL_PATH='/house/graphql'
GRAPHQL_API_URL="${SITE_BASE_URI}${GRAPHQL_PATH}"
SITE_COMMAND='../site'
$SITE_COMMAND -s check-token || echo Getting a token from $SITE_BASE_URI && $SITE_COMMAND get-token -u admin -p admin -b $SITE_BASE_URI
echo Deploying schema to $GRAPHQL_PATH
$SITE_COMMAND post-resources --file resources.edn -b $SITE_BASE_URI
$SITE_COMMAND put-graphql --file schema.graphql --path $GRAPHQL_PATH -b $SITE_BASE_URI
