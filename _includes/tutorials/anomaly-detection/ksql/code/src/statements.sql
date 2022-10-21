CREATE TABLE suspicious_names (CREATED_TS VARCHAR,
                               COMPANY_NAME VARCHAR PRIMARY KEY,
                               COMPANY_ID INT)
    WITH (kafka_topic='suspicious_names',
          partitions=1,
          value_format='JSON',
          timestamp='CREATED_TS',
          timestamp_format = 'yyyy-MM-dd HH:mm:ss');

CREATE STREAM transactions (TXN_ID BIGINT, USERNAME VARCHAR, RECIPIENT VARCHAR, AMOUNT DOUBLE, TS VARCHAR)
    WITH (kafka_topic='transactions',
          partitions=1,
          value_format='JSON',
          timestamp='TS',
          timestamp_format = 'yyyy-MM-dd HH:mm:ss');

CREATE STREAM suspicious_transactions
    WITH (kafka_topic='suspicious_transactions', partitions=1, value_format='JSON') AS
    SELECT T.TXN_ID, T.USERNAME, T.RECIPIENT, T.AMOUNT, T.TS
    FROM transactions T
    INNER JOIN
    suspicious_names S
    ON T.RECIPIENT = S.COMPANY_NAME;

CREATE TABLE accounts_to_monitor
    WITH (kafka_topic='accounts_to_monitor', partitions=1, value_format='JSON') AS
    SELECT USERNAME,
           TIMESTAMPTOSTRING(WINDOWSTART, 'yyyy-MM-dd HH:mm:ss Z') AS WINDOW_START,
           TIMESTAMPTOSTRING(WINDOWEND, 'yyyy-MM-dd HH:mm:ss Z') AS WINDOW_END
    FROM suspicious_transactions
    WINDOW TUMBLING (SIZE 24 HOURS)
    GROUP BY USERNAME
    HAVING COUNT(*) > 3;