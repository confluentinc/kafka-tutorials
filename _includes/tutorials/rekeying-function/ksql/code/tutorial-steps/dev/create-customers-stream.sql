CREATE STREAM customers (id int, firstname string, lastname string, phonenumber string)
  WITH (kafka_topic='customers',
        partitions=2,
        key='id',
        value_format = 'avro');