CREATE STREAM flight_updates (
  id INT KEY,
  flight_id INT,
  updated_dep TIMESTAMP,
  reason VARCHAR
) WITH (
  KAFKA_TOPIC = 'flight_updates',
  FORMAT = 'JSON',
  PARTITIONS = 6
);
