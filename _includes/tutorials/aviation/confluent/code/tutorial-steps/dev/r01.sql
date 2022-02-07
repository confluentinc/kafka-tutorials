CREATE STREAM cf_stream WITH (KAFKA_TOPIC='customer_flights', FORMAT='JSON');

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

CREATE TABLE customer_flights_rekeyed 
  (flight_id INT PRIMARY KEY) 
  WITH (KAFKA_TOPIC='cf_rekey', FORMAT='JSON');
