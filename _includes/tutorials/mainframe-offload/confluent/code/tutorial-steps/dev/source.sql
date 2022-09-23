CREATE SOURCE CONNECTOR IF NOT EXISTS recipe_ibmmq_transactions WITH (
  'connector.class'          = 'IbmMQSource',
  'kafka.api.key'            = '<my-kafka-api-key>',
  'kafka.api.secret'         = '<my-kafka-api-secret>',
  'kafka.topic'              = 'mq_transactions',
  'output.data.format'       = 'JSON',
  'jms.destination.name'     = '<destination-name>',
  'mq.username'              = '<authorized-user>',
  'mq.password'              = '<user-password>',
  'mq.hostname'              = '<server-hostname>',
  'mq.queue.manager'         = '<queue-name>',
  'tasks.max'                = '1'
);
