CREATE TABLE customer_bookings WITH (KAFKA_TOPIC = 'customer_bookings', KEY_FORMAT = 'KAFKA', VALUE_FORMAT = 'JSON') AS
  SELECT C.*,
         B.id,
         B.flight_id
  FROM bookings B
  INNER JOIN customers C
  ON B.customer_id = C.id;
