SELECT TEMPERATURE, CONVERT_TZ(EVENTTIME_TS, 'UTC', 'America/Denver') AS EVENTTIME_MT
FROM TEMPERATURE_READINGS_TIMESTAMP
EMIT CHANGES
LIMIT 6;
