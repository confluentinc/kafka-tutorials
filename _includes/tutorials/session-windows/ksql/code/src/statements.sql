CREATE STREAM clicks (ip VARCHAR, url VARCHAR, timestamp VARCHAR)
WITH (KAFKA_TOPIC='clicks',
      TIMESTAMP='timestamp',
      TIMESTAMP_FORMAT='yyyy-MM-dd''T''HH:mm:ssX',
      PARTITIONS=1,
      VALUE_FORMAT='Avro');

CREATE TABLE IP_SESSIONS AS
SELECT IP, 
       TIMESTAMPTOSTRING(WINDOWSTART,'yyyy-MM-dd HH:mm:ss') AS SESSION_START_TS, 
       TIMESTAMPTOSTRING(WINDOWEND,'yyyy-MM-dd HH:mm:ss')   AS SESSION_END_TS, 
       COUNT(*)                                             AS CLICK_COUNT, 
       WINDOWEND - WINDOWSTART                              AS SESSION_LENGTH_MS 
  FROM CLICKS 
       WINDOW SESSION (5 MINUTES) 
GROUP BY IP;
