CREATE STREAM customers (rowkey int key, firstname string, lastname string, phonenumber string)
  WITH (kafka_topic='customers',
        partitions=2,
        value_format = 'avro');

CREATE STREAM customers_with_area_code AS
    SELECT
      rowkey,
      firstname,
      lastname,
      phonenumber,
      REGEXREPLACE(phonenumber, '\\(?(\\d{3}).*', '$1') as area_code
    FROM customers;

CREATE STREAM customers_by_area_code
  WITH (KAFKA_TOPIC='customers_by_area_code') AS
    SELECT * from customers_with_area_code
    PARTITION BY area_code
    EMIT CHANGES;
