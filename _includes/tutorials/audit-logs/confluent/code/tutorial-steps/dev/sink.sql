-- Send data to Splunk
CREATE SINK CONNECTOR filtered_splunk WITH (
  'connector.class'          = 'SplunkSink',
  'name'                     = 'recipe-splunk-filter-logs',
  'input.data.format'        = 'JSON',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'topics'                   = 'TOPIC-OPERATIONS-AUDIT-LOG',
  'splunk.hec.uri'           = '<splunk-indexers>',
  'splunk.hec.token'         = '<Splunk HTTP Event Collector token>',
  'tasks.max'                = '1'
);
