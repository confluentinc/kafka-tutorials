CREATE STREAM transactions (TXN_ID BIGINT, USERNAME VARCHAR, RECIPIENT VARCHAR, AMOUNT DOUBLE, TS VARCHAR)
    WITH (kafka_topic='transactions',
          partitions=1,
          value_format='JSON',
          timestamp='TS',
          timestamp_format = 'yyyy-MM-dd HH:mm:ss');
