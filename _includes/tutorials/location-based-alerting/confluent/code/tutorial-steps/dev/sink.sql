CREATE SINK CONNECTOR IF NOT EXISTS promo-alerts-sink WITH (
  'connector.class'    = 'ElasticsearchSink',
  'name'               = 'promo-alerts-sink',
  'input.data.format'  = 'JSON',
  'kafka.api.key'      = '<my-kafka-api-key>',
  'kafka.api.secret'   = '<my-kafka-api-secret>',
  'topics'             = 'promo-alerts',
  'connection.url'     = '<elasticsearch-URI>',
  'connection.username'= '<elasticsearch-username>',
  'connection.password'= '<elasticsearch-password>',
  'type.name'          = 'type.name=kafkaconnect',
  'key.ignore'         = 'true',
  'schema.ignore'      = 'true',
  'tasks.max'          = '1'
);
