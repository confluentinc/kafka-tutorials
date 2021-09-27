SET 'auto.offset.reset' = 'earliest';

SELECT TIMESTAMPTOSTRING(ROWTIME,'yyyy-MM-dd HH:mm:ss','Europe/London') AS CLICK_TS, 
       SOURCE_IP, 
       HOST, 
       PATH 
  FROM CLICKS 
 WHERE USER_AGENT LIKE 'Moz%'
 EMIT CHANGES
 LIMIT 5;
