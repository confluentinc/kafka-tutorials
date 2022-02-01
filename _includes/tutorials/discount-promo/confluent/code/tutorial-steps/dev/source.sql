CREATE SOURCE CONNECTOR discount_codes WITH (
  'connector.class'          = 'PostgresSource',
  'name'                     = 'recipe-postgres-discounting-codes',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'connection.host'          = '<my-database-endpoint>',
  'connection.port'          = '5432',
  'connection.user'          = 'postgres',
  'connection.password'      = '<my-database-password>',
  'db.name'                  = '<db-name>',
  'table.whitelist'          = 'discount_codes',
  'timestamp.column.name'    = 'timestamp',
  'output.data.format'       = 'JSON',
  'db.timezone'              = 'UTC',
  'tasks.max'                = '1'
);

CREATE SOURCE CONNECTOR orders WITH (
  'connector.class'          = 'PostgresSource',
  'name'                     = 'recipe-postgres-discounting-orders',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'connection.host'          = '<my-database-endpoint>',
  'connection.port'          = '5432',
  'connection.user'          = 'postgres',
  'connection.password'      = '<my-database-password>',
  'db.name'                  = '<db-name>',
  'table.whitelist'          = 'orders',
  'timestamp.column.name'    = 'order_time',
  'output.data.format'       = 'JSON',
  'db.timezone'              = 'UTC',
  'tasks.max'                = '1'
);
