CREATE SOURCE CONNECTOR IF NOT EXISTS merchant_data_cdc WITH (
  'connector.class'       = 'PostgresSource',
  'name'                  = 'merchant_data_cdc',
  'kafka.api.key'         = '<my-kafka-api-key>',
  'kafka.api.secret'      = '<my-kafka-api-secret>',
  'connection.host'       = '<database-host>',
  'connection.port'       = '5432',
  'connection.user'       = 'postgres',
  'connection.password'   = '<database-password>',
  'db.name'               = '<db-name>',
  'table.whitelist'       = 'merchant-locations',
  'timestamp.column.name' = 'created_at',
  'output.data.format'    = 'JSON',
  'db.timezone'           = 'UTC',
  'tasks.max'             = '1'
);
