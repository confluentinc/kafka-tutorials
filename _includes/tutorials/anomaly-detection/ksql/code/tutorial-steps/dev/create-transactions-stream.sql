CREATE STREAM transactions (TXN_ID BIGINT, USERNAME VARCHAR, RECIPIENT VARCHAR, AMOUNT DOUBLE)
    WITH (kafka_topic='transactions', partitions=1, value_format='JSON');
