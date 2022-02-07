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
