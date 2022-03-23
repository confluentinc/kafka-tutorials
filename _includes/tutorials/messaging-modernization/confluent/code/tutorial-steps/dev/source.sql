CREATE SOURCE CONNECTOR IF NOT EXISTS RabbitMQ WITH (
  'connector.class'          = 'RabbitMQSource',
  'name'                     = 'RabbitMQSource_0',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'kafka.topic'              = 'from-rabbit'
  'rabbitmq.host'            = '<host>',
  'rabbitmq.username'        = '<username>',
  'rabbitmq.password'        = '<password>',
  'rabbitmq.queue'           = 'transactions',
  'tasks.max'                = '1'
);
