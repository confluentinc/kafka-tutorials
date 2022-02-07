CREATE TABLE customer_bookings AS 
  SELECT C.*, B.id, B.flight_id
  FROM   bookings B
          INNER JOIN customers C
              ON B.customer_id = C.id;
