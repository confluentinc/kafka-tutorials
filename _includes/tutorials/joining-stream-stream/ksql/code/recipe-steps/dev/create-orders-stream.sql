CREATE STREAM ORDERS (ORDER_ID INT, ORDER_TS VARCHAR, TOTAL_AMOUNT DOUBLE, CUSTOMER_NAME VARCHAR)
              WITH (KAFKA_TOPIC='_orders',
                    VALUE_FORMAT='JSON',
                    TIMESTAMP='ORDER_TS',
                    TIMESTAMP_FORMAT='yyyy-MM-dd''T''HH:mm:ssX',
                    PARTITIONS=4,
                    REPLICAS=1);
                    