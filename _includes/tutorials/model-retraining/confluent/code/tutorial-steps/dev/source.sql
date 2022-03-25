-- Stream of fish weight predictions
CREATE SOURCE CONNECTOR IF NOT EXISTS weight_predictions WITH (
  'connector.class'        = 'MongoDbAtlasSource',
  'name'                   = 'model-retrain-weight_predictions',
  'kafka.api.key'          = '<my-kafka-api-key>',
  'kafka.api.secret'       = '<my-kafka-api-secret>',
  'connection.host'        = '<database-host-address>',
  'connection.user'        = '<database-username>',
  'connection.password'    = '<database-password>',
  'topic.prefix'           = 'kt',
  'database'               = 'mdb',
  'collection'             = 'weight-prediction',
  'poll.await.time.ms'     = '5000',
  'poll.max.batch.size'    = '1000',
  'copy.existing'          = 'true',
  'output.data.format'     = 'JSON',
  'tasks.max'              = '1'
);

-- Stream of actual fish weights
CREATE SOURCE CONNECTOR IF NOT EXISTS actual_weights WITH (
  'connector.class'        = 'MongoDbAtlasSource',
  'name'                   = 'model-retrain-actual_weights',
  'kafka.api.key'          = '<my-kafka-api-key>',
  'kafka.api.secret'       = '<my-kafka-api-secret>',
  'connection.host'        = '<database-host-address>',
  'connection.user'        = '<database-username>',
  'connection.password'    = '<database-password>',
  'topic.prefix'           = 'kt',
  'database'               = 'mdb',
  'collection'             = 'machine-weight',
  'poll.await.time.ms'     = '5000',
  'poll.max.batch.size'    = '1000',
  'copy.existing'          = 'true',
  'output.data.format'     = 'JSON',
  'tasks.max'              = '1'
);

