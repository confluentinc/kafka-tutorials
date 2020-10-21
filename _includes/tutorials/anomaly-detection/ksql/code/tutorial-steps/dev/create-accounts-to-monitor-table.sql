CREATE TABLE accounts_to_monitor
  WITH (kafka_topic='accounts_to_monitor', partitions=1, value_format='JSON') AS
  SELECT TIMESTAMPTOSTRING(WINDOWSTART, 'yyyy-MM-dd HH:mm:ss Z') AS WINDOW_START,
    TIMESTAMPTOSTRING(WINDOWEND, 'yyyy-MM-dd HH:mm:ss Z') AS WINDOW_END, USERNAME, COUNT(*) AS TXN_COUNT
  FROM suspicious_transactions
  WINDOW TUMBLING (SIZE 24 HOURS)
  GROUP BY USERNAME
  HAVING COUNT(*) > 3;
