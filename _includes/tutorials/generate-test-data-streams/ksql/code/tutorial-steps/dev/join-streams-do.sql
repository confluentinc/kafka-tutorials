SELECT TIMESTAMPTOSTRING(T.ROWTIME,'yyyy-MM-dd HH:mm:ss','Europe/London') AS EVENT_TS, 
       MAC, 
       BYTES_SENT, 
       NAME, 
       LOCATION 
  FROM TRAFFIC T 
         LEFT JOIN 
       DEVICES D 
         ON T.MAC=D.ID
EMIT CHANGES LIMIT 10;
