-- Stream of users
CREATE SOURCE CONNECTOR datagen_clickstream_users WITH (
  'name'                     = 'Datagen_users',
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
CREATE SOURCE CONNECTOR datagen_clickstream WITH (
  'name'                     = 'Datagen_clicks',
  'connector.class'          = 'DatagenSource',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'kafka.topic'              = 'clickstream',
  'quickstart'               = 'CLICKSTREAM',
  'maxInterval'              = '30',
  'tasks.max'                = '1',
  'output.data.format'       = 'JSON'
);
