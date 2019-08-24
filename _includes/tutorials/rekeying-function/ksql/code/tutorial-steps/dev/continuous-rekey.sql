CREATE STREAM customers_by_area_code
  WITH (KAFKA_TOPIC='customers_by_area_code') AS
    SELECT 
      id,
      firstname,
      lastname,
      phonenumber,
      REGEXREPLACE(phonenumber, '\\(?(\\d{3}).*', '$1') as area_code
    FROM customers
    PARTITION BY area_code;
