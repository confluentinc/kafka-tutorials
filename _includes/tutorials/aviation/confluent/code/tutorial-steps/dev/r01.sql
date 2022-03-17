CREATE STREAM cf_stream (
  cb_c_id INTEGER,
  cb_c_name VARCHAR,
  cb_c_address VARCHAR,
  cb_c_email VARCHAR,
  cb_c_phone VARCHAR,
  cb_c_loyalty_status VARCHAR,
  cb_c_loyalty_id VARCHAR,
  cb_flight_id INTEGER,
  f_id INTEGER,
  f_origin VARCHAR,
  f_destination VARCHAR,
  f_code VARCHAR,
  f_scheduled_dep TIMESTAMP,
  f_scheduled_arr TIMESTAMP
) WITH (
  KAFKA_TOPIC = 'customer_flights',
  KEY_FORMAT = 'KAFKA',
  VALUE_FORMAT = 'JSON'
);

CREATE STREAM cf_rekey_masked WITH (KAFKA_TOPIC = 'cf_rekey_masked') AS
  SELECT f_id           AS flight_id,
    cb_c_id             AS customer_id,
    cb_c_name           AS customer_name,
    cb_c_address        AS customer_address,
    cb_c_email          AS customer_email,
    cb_c_phone          AS customer_phone,
    cb_c_loyalty_status AS customer_loyalty_status,
    MASK_KEEP_RIGHT(cb_c_loyalty_id,3) AS customer_loyalty_id,
    f_origin            AS flight_origin,
    f_destination       AS flight_destination,
    f_code              AS flight_code,
    f_scheduled_dep     AS flight_scheduled_dep,
    f_scheduled_arr     AS flight_scheduled_arr
  FROM cf_stream
  PARTITION BY f_id;

CREATE TABLE customer_flights_rekeyed (
  flight_id INT PRIMARY KEY,
  customer_id VARCHAR,
  customer_name VARCHAR,
  customer_address VARCHAR,
  customer_email VARCHAR,
  customer_phone VARCHAR,
  customer_loyalty_status VARCHAR,
  customer_loyalty_id VARCHAR,
  flight_origin VARCHAR,
  flight_destination VARCHAR,
  flight_code VARCHAR,
  flight_scheduled_dep TIMESTAMP,
  flight_scheduled_arr TIMESTAMP
) WITH (
  KAFKA_TOPIC = 'cf_rekey_masked',
  KEY_FORMAT = 'KAFKA',
  VALUE_FORMAT = 'JSON'
);
