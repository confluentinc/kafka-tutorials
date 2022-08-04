CREATE STREAM transactions (TXN_ID BIGINT, USERNAME VARCHAR, RECIPIENT VARCHAR, AMOUNT DOUBLE, TS TIMESTAMP)
    WITH (kafka_topic='transactions',
          partitions=1,
          value_format='JSON',
          timestamp='TS');
