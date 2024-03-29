-- Stream of campaign contributions
CREATE SOURCE CONNECTOR IF NOT EXISTS recipe_datagen_campaign_finance WITH (
  'connector.class'          = 'DatagenSource',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'kafka.topic'              = 'campaign_finance',
  'quickstart'               = 'CAMPAIGN_FINANCE',
  'maxInterval'              = '10',
  'tasks.max'                = '1',
  'output.data.format'       = 'JSON'
);
