-- Stream of transactions
CREATE SOURCE CONNECTOR IF NOT EXISTS FD_transactions WITH (
  'connector.class'          = 'OracleDatabaseSource',
  'name'                     = 'recipe-oracle-transactions-cc',
  'connector.class'          = 'OracleDatabaseSource',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'connection.host'          = '<database-host>',
  'connection.port'          = '1521',
  'connection.user'          = '<database-username>',
  'connection.password'      = '<database-password>',
  'db.name'                  = '<db-name>',
  'table.whitelist'          = 'TRANSACTIONS',
  'timestamp.column.name'    = 'created_at',
  'output.data.format'       = 'JSON',
  'db.timezone'              = 'UCT',
  'tasks.max'                = '1'
);

-- Stream of customers
CREATE SOURCE CONNECTOR IF NOT EXISTS FD_customers WITH (
  'connector.class'          = 'OracleDatabaseSource',
  'name'                     = 'recipe-oracle-customers-cc',
  'connector.class'          = 'OracleDatabaseSource',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'connection.host'          = '<database-host>',
  'connection.port'          = '1521',
  'connection.user'          = '<database-username>',
  'connection.password'      = '<database-password>',
  'db.name'                  = '<db-name>',
  'table.whitelist'          = 'CUSTOMERS',
  'timestamp.column.name'    = 'created_at',
  'output.data.format'       = 'JSON',
  'db.timezone'              = 'UCT',
  'tasks.max'                = '1'
);
