CREATE SOURCE CONNECTOR IF NOT EXISTS campaign_finance WITH (
  'connector.class'       = 'MySqlCdcSource',
  'name'                  = 'campaign_finance',
  'kafka.api.key'         = '<my-kafka-api-key>',
  'kafka.api.secret'      = '<my-kafka-api-secret>',
  'database.hostname'     = '<db-hostname>',
  'database.port'         = '3306',
  'database.user'         = '<db-user>',
  'database.password'     = '<db-password>',
  'database.server.name'  = 'mysql',
  'database.whitelist'    = 'campaign_finance',
  'table.includelist'     = 'transactions',
  'snapshot.mode'         = 'initial',
  'output.data.format'    = 'JSON',
  'tasks.max'             = '1'
);
