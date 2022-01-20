CREATE TABLE customer_flights 
  WITH (KAFKA_TOPIC='customer_flights') AS
  SELECT CB.*, F.*
  FROM   customer_bookings CB
          INNER JOIN flights F
              ON CB.FLIGHT_ID=F.ID;
