-- Send data to Snowflake
CREATE SINK CONNECTOR IF NOT EXISTS customer_flight_updates WITH (
  'connector.class'          = 'SnowflakeSink',
  'name'                     = 'recipe-snowflake-aviation',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'topics'                   = 'customer_flight_updates',
  'input.data.format'        = 'JSON',
  'snowflake.url.name'       = 'https=//wm83168.us-central1.gcp.snowflakecomputing.com=443',
  'snowflake.user.name'      = '<login-username>',
  'snowflake.private.key'    = '<private-key>',
  'snowflake.database.name'  = '<database-name>',
  'snowflake.schema.name'    = '<schema-name>',
  'tasks.max'                = '1'
);

-- Send data to Amazon Lambda
CREATE SINK CONNECTOR IF NOT EXISTS recipe-lambda-aviation WITH (
  'connector.class'          = 'LambdaSink',
  'name'                     = 'recipe-lambda-aviation',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'topics'                   = 'customer_flight_updates',
  'input.data.format'        = 'JSON',
  'aws.access.key.id'        = '<aws-key-id>',
  'aws.secret.access.key'    = '<aws-access-key>',
  'aws.lambda.function.name' = 'lambdaTextCustomer',
  'aws.lambda.invocation.type' = 'sync',
  'behavior.on.error'        = 'fail',
  'tasks.max'                = '1'
);
