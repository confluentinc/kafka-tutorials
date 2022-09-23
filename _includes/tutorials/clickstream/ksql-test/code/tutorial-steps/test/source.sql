-- Stream of users
CREATE SOURCE CONNECTOR IF NOT EXISTS recipe_datagen_users WITH (
  'connector.class'          = 'io.confluent.kafka.connect.datagen.DatagenConnector',
  'kafka.topic'              = 'clickstream_users',
  'quickstart'               = 'CLICKSTREAM_USERS',
  'maxInterval'              = '10',
  'tasks.max'                = '1',
  'output.data.format'       = 'JSON'
);

-- Stream of per-user session information
CREATE SOURCE CONNECTOR IF NOT EXISTS recipe_datagen_clicks WITH (
  'connector.class'          = 'io.confluent.kafka.connect.datagen.DatagenConnector',
  'kafka.topic'              = 'clickstream',
  'quickstart'               = 'CLICKSTREAM',
  'maxInterval'              = '30',
  'tasks.max'                = '1',
  'output.data.format'       = 'JSON'
);
