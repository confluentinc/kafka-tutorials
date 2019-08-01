CREATE STREAM SHIPMENTS (SHIPMENT_ID VARCHAR, SHIP_TS VARCHAR, ORDER_ID INT, WAREHOUSE VARCHAR)
              WITH (KAFKA_TOPIC='_shipments',
                    VALUE_FORMAT='JSON',
                    TIMESTAMP='SHIP_TS',
                    TIMESTAMP_FORMAT='yyyy-MM-dd''T''HH:mm:ssX',
                    PARTITIONS=4,
                    REPLICAS=1);
                    