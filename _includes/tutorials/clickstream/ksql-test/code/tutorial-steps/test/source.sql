-- Stream of users
CREATE SOURCE CONNECTOR IF NOT EXISTS DATAGEN_USERS WITH (
  'name'                     = 'DATAGEN_USERS',
  'connector.class'          = 'io.confluent.kafka.connect.datagen.DatagenConnector',
  'kafka.topic'              = 'clickstream_users',
  'quickstart'               = 'CLICKSTREAM_USERS',
  'maxInterval'              = '10',
  'tasks.max'                = '1',
  'output.data.format'       = 'JSON'
);

-- Stream of per-user session information
CREATE SOURCE CONNECTOR IF NOT EXISTS DATAGEN_CLICKS WITH (
  'name'                     = 'DATAGEN_CLICKS',
  'connector.class'          = 'io.confluent.kafka.connect.datagen.DatagenConnector',
  'kafka.topic'              = 'clickstream',
  'quickstart'               = 'CLICKSTREAM',
  'maxInterval'              = '30',
  'tasks.max'                = '1',
  'output.data.format'       = 'JSON'
);
