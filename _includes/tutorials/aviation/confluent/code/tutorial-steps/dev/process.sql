SET 'auto.offset.reset' = 'earliest';

CREATE TABLE customers (
  id INT PRIMARY KEY,
  name VARCHAR,
  address VARCHAR,
  email VARCHAR,
  phone VARCHAR,
  loyalty_status VARCHAR
) WITH (
  KAFKA_TOPIC='customers',
  FORMAT='JSON',
  PARTITIONS=6
);

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

CREATE TABLE customer_bookings AS 
  SELECT C.*, B.id, B.flight_id
  FROM bookings B
  INNER JOIN customers C
  ON B.customer_id = C.id;

CREATE TABLE customer_flights WITH (KAFKA_TOPIC = 'customer_flights') AS
  SELECT CB.*, F.*
  FROM customer_bookings CB
  INNER JOIN flights F
  ON CB.flight_id = F.id;

CREATE STREAM cf_stream WITH (KAFKA_TOPIC = 'customer_flights', FORMAT = 'JSON');

CREATE STREAM cf_rekey WITH (KAFKA_TOPIC = 'cf_rekey') AS 
  SELECT f_id AS flight_id,
    cb_c_id             AS customer_id,
    cb_c_name           AS customer_name,
    cb_c_address        AS customer_address,
    cb_c_email          AS customer_email,
    cb_c_phone          AS customer_phone,
    cb_c_loyalty_status AS customer_loyalty_status,
    f_origin            AS flight_origin,
    f_destination       AS flight_destination,
    f_code              AS flight_code,
    f_scheduled_dep     AS flight_scheduled_dep,
    f_scheduled_arr     AS flight_scheduled_arr
  FROM cf_stream
  PARTITION BY f_id;

CREATE TABLE customer_flights_rekeyed (flight_id INT PRIMARY KEY) 
WITH (KAFKA_TOPIC = 'cf_rekey', FORMAT = 'JSON');

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

CREATE STREAM customer_flight_updates AS
SELECT  customer_name,
  FU.reason AS flight_change_reason,
  FU.updated_dep AS flight_updated_dep,
  flight_scheduled_dep,
  customer_email,
  customer_phone,
  flight_destination,
  flight_code
FROM flight_updates FU
  INNER JOIN customer_flights_rekeyed CB
  ON FU.flight_id = CB.flight_id
EMIT CHANGES;
