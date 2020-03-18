CREATE STREAM customers_by_area_code
  WITH (KAFKA_TOPIC='customers_by_area_code') AS
    SELECT * from customers_with_area_code
    PARTITION BY area_code
    EMIT CHANGES;
