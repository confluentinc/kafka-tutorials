CREATE SOURCE CONNECTOR IF NOT EXISTS orders WITH (
  'connector.class'          = 'PostgresSource',
  'name'                     = 'recipe-postgres-logistics-orders',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'connection.host'          = '<database-host>',
  'connection.port'          = '5432',
  'connection.user'          = 'postgres',
  'connection.password'      = '<database-password>',
  'db.name'                  = '<db-name>',
  'table.whitelist'          = 'orders',
  'timestamp.column.name'    = 'timestamp',
  'output.data.format'       = 'JSON',
  'db.timezone'              = 'UTC',
  'tasks.max'                = '1'
);

CREATE SOURCE CONNECTOR IF NOT EXISTS fleet_updates WITH (
  'connector.class'          = 'PostgresSource',
  'name'                     = 'recipe-postgres-logistics-fleet',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'connection.host'          = '<database-host>',
  'connection.port'          = '5432',
  'connection.user'          = 'postgres',
  'connection.password'      = '<database-password>',
  'db.name'                  = '<db-name>',
  'table.whitelist'          = 'fleet_updates',
  'timestamp.column.name'    = 'timestamp',
  'output.data.format'       = 'JSON',
  'db.timezone'              = 'UTC',
  'tasks.max'                = '1'
);