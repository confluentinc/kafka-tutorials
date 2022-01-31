-- Example
CREATE SOURCE CONNECTOR online_dating WITH (
  'connector.class'          = 'PostgresSource',
  'name'                     = 'recipe-postgres-dating',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'connection.host'          = '<database-endpoint>',
  'connection.port'          = '<database-endpoint>',
  'connection.user'          = '<database-user>',
  'connection.password'      = '<database-password>',
  'database'                 = '<database-name>',
  'table.whitelist'          : 'messages',
  'timestamp.column.name'    : 'created_at',
  'output.data.format'       : 'JSON',
  'db.timezone'              : 'UTC',
  'tasks.max'                : '1'
);
