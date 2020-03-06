CREATE STREAM customers_with_area_code AS
    SELECT 
      customers.rowkey as id,
      firstname,
      lastname,
      phonenumber,
      REGEXREPLACE(phonenumber, '\\(?(\\d{3}).*', '$1') as area_code
    FROM customers;
