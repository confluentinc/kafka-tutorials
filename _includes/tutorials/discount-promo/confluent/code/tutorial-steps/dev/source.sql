CREATE SOURCE CONNECTOR IF NOT EXISTS discount_codes WITH (
  'connector.class'          = 'PostgresSource',
  'name'                     = 'recipe-postgres-discounting-codes',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'connection.host'          = '<database-host>',
  'connection.port'          = '5432',
  'connection.user'          = 'postgres',
  'connection.password'      = '<database-password>',
  'db.name'                  = '<db-name>',
  'table.whitelist'          = 'discount_codes',
  'timestamp.column.name'    = 'timestamp',
  'output.data.format'       = 'JSON',
  'db.timezone'              = 'UTC',
  'tasks.max'                = '1'
);

CREATE SOURCE CONNECTOR IF NOT EXISTS orders WITH (
  'connector.class'          = 'PostgresSource',
  'name'                     = 'recipe-postgres-discounting-orders',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'connection.host'          = '<database-host>',
  'connection.port'          = '5432',
  'connection.user'          = 'postgres',
  'connection.password'      = '<database-password>',
  'db.name'                  = '<db-name>',
  'table.whitelist'          = 'order_data',
  'timestamp.column.name'    = 'order_time',
  'output.data.format'       = 'JSON',
  'db.timezone'              = 'UTC',
  'tasks.max'                = '1'
);
