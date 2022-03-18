CREATE TABLE customers (
  id INT PRIMARY KEY,
  name           VARCHAR,
  address        VARCHAR,
  email          VARCHAR,
  phone          VARCHAR,
  loyalty_status VARCHAR,
  loyalty_id     VARCHAR
) WITH (
  KAFKA_TOPIC = 'customers',
  FORMAT = 'JSON',
  PARTITIONS = 6
);
