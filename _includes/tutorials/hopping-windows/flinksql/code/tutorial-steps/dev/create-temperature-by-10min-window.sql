CREATE TABLE temperature_by_10min_window
WITH (
    'connector' = 'kafka',
    'topic' = 'temperature-by-10min-window',
    'properties.bootstrap.servers' = 'broker:9092',
    'scan.startup.mode' = 'earliest-offset',
    'key.format' = 'avro-confluent',
    'key.avro-confluent.url' = 'http://schema-registry:8081',
    'key.fields' = 'sensor_id;window_start;window_end',
    'value.format' = 'avro-confluent',
    'value.avro-confluent.url' = 'http://schema-registry:8081',
    'value.fields-include' = 'ALL'
) AS
    SELECT sensor_id,
        AVG(temperature) AS avg_temperature,
        window_start,
        window_end
    FROM TABLE(HOP(TABLE temperature_readings, DESCRIPTOR(ts), INTERVAL '5' MINUTES, INTERVAL '10' MINUTES))
    GROUP BY sensor_id, window_start, window_end;
