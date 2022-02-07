CREATE TABLE customer_flights WITH (KAFKA_TOPIC = 'customer_flights', KEY_FORMAT = 'KAFKA', VALUE_FORMAT = 'JSON') AS
  SELECT CB.*,
         F.*
  FROM customer_bookings CB
  INNER JOIN flights F
  ON CB.flight_id = F.id;
