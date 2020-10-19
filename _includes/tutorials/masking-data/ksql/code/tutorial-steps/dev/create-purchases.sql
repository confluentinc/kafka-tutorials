CREATE STREAM purchases (order_id INT, customer_name VARCHAR, date_of_birth VARCHAR,
                         product VARCHAR, order_total_usd DOUBLE, town VARCHAR, country VARCHAR)
       WITH (KAFKA_TOPIC='purchases', VALUE_FORMAT='JSON', PARTITIONS=1);