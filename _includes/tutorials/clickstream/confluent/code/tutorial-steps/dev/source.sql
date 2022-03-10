-- Stream of users
CREATE SOURCE CONNECTOR DATAGEN_USERS WITH (
  'name'                     = 'DATAGEN_USERS',
  'connector.class'          = 'DatagenSource',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'kafka.topic'              = 'clickstream_users',
  'quickstart'               = 'CLICKSTREAM_USERS',
  'maxInterval'              = '10',
  'tasks.max'                = '1',
  'output.data.format'       = 'JSON'
);

-- Stream of per-user session information
CREATE SOURCE CONNECTOR DATAGEN_CLICKS WITH (
  'name'                     = 'DATAGEN_CLICKS',
  'connector.class'          = 'DatagenSource',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'kafka.topic'              = 'clickstream',
  'quickstart'               = 'CLICKSTREAM',
  'maxInterval'              = '30',
  'tasks.max'                = '1',
  'output.data.format'       = 'JSON'
);
