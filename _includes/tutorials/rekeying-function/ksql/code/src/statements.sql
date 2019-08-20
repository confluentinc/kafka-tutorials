CREATE STREAM customers (id int, firstname string, lastname string, phonenumber string)
  WITH (kafka_topic='customers',
        partitions=2,
        key='id',
        value_format = 'avro');

CREATE STREAM customers_by_area_code
  WITH (KAFKA_TOPIC='customers_by_area_code') AS
    SELECT 
      id,
      firstname,
      lastname,
      REGEXREPLACE(phonenumber, '\\(?(\\d{3}).*', '$1') as area_code
    FROM customers
    PARTITION BY area_code;
