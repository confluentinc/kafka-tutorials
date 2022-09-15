-- Stream of products (shoes)
CREATE SOURCE CONNECTOR IF NOT EXISTS DATAGEN_SHOES WITH (
  'name'                     = 'DATAGEN_SHOES',
  'connector.class'          = 'DatagenSource',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'kafka.topic'              = 'shoes',
  'quickstart'               = 'SHOES',
  'maxInterval'              = '10',
  'tasks.max'                = '1',
  'output.data.format'       = 'JSON'
);

-- Stream of customers
CREATE SOURCE CONNECTOR IF NOT EXISTS DATAGEN_SHOE_CUSTOMERS WITH (
  'name'                     = 'DATAGEN_SHOE_CUSTOMERS',
  'connector.class'          = 'DatagenSource',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'kafka.topic'              = 'shoe_customers',
  'quickstart'               = 'SHOE_CUSTOMERS',
  'maxInterval'              = '10',
  'tasks.max'                = '1',
  'output.data.format'       = 'JSON'
);

-- Stream of orders
CREATE SOURCE CONNECTOR IF NOT EXISTS DATAGEN_SHOE_ORDERS WITH (
  'name'                     = 'DATAGEN_SHOE_ORDERS',
  'connector.class'          = 'DatagenSource',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'kafka.topic'              = 'shoe_orders',
  'quickstart'               = 'SHOE_ORDERS',
  'maxInterval'              = '10',
  'tasks.max'                = '1',
  'output.data.format'       = 'JSON'
);

-- Stream of ecommerce website clicks
CREATE SOURCE CONNECTOR IF NOT EXISTS DATAGEN_SHOE_CLICKSTREAM WITH (
  'name'                     = 'DATAGEN_SHOE_CLICKSTREAM',
  'connector.class'          = 'DatagenSource',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'kafka.topic'              = 'shoe_clickstream',
  'quickstart'               = 'SHOE_CLICKSTREAM',
  'maxInterval'              = '30',
  'tasks.max'                = '1',
  'output.data.format'       = 'JSON'
);
