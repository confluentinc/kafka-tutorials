SET 'ksql.streams.cache.max.bytes.buffering'='0';
SELECT IP, 
       FORMAT_TIMESTAMP(FROM_UNIXTIME(WINDOWSTART),'yyyy-MM-dd HH:mm:ss', 'UTC') AS SESSION_START_TS,
       FORMAT_TIMESTAMP(FROM_UNIXTIME(WINDOWEND),'yyyy-MM-dd HH:mm:ss', 'UTC')   AS SESSION_END_TS,
       COUNT(*)                                                    AS CLICK_COUNT,
       WINDOWEND - WINDOWSTART                                     AS SESSION_LENGTH_MS
  FROM CLICKS 
       WINDOW SESSION (5 MINUTES) 
GROUP BY IP
EMIT CHANGES
LIMIT 9;
