CREATE STREAM shipments (shipment_id VARCHAR, ship_ts VARCHAR, order_id INT, warehouse VARCHAR)
    WITH (KAFKA_TOPIC='_shipments',
          VALUE_FORMAT='AVRO',
          TIMESTAMP='ship_ts',
          TIMESTAMP_FORMAT='yyyy-MM-dd''T''HH:mm:ssX',
          PARTITIONS=4);
