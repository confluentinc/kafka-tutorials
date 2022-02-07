CREATE TABLE flights (
  id INT PRIMARY KEY,
  origin VARCHAR,
  destination VARCHAR,
  code VARCHAR,
  scheduled_dep TIMESTAMP,
  scheduled_arr TIMESTAMP
) WITH (
  KAFKA_TOPIC='flights',
  FORMAT='JSON',
  PARTITIONS=6
);

CREATE TABLE bookings (
  id INT PRIMARY KEY,
  customer_id INT,
  flight_id INT
) WITH (
  KAFKA_TOPIC='bookings',
  FORMAT='JSON',
  PARTITIONS=6
);
