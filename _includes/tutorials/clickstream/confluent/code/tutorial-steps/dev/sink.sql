-- Send data to Elasticsearch
CREATE SINK CONNECTOR IF NOT EXISTS recipe_elasticsearch_analyzed_clickstream WITH (
  'connector.class'          = 'ElasticsearchSink',
  'input.data.format'        = 'JSON',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'topics'                   = 'user_ip_activity, errors_per_min_alert',
  'connection.url'           = '<elasticsearch-URI>',
  'connection.user'          = '<elasticsearch-username>',
  'connection.password'      = '<elasticsearch-password>',
  'type.name'                = 'type.name=kafkaconnect',
  'key.ignore'               = 'true',
  'schema.ignore'            = 'true'
);
