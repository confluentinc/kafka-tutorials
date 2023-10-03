CREATE TABLE revenue_per_hour_cumulating
WITH (
    'connector' = 'kafka',
    'topic' = 'revenue-per-hour-cumulating',
    'properties.bootstrap.servers' = 'broker:9092',
    'scan.startup.mode' = 'earliest-offset',
    'key.format' = 'avro-confluent',
    'key.avro-confluent.url' = 'http://schema-registry:8081',
    'key.fields' = 'window_start;window_end',
    'value.format' = 'avro-confluent',
    'value.avro-confluent.url' = 'http://schema-registry:8081',
    'value.fields-include' = 'ALL'
) AS
    SELECT ROUND(SUM(quantity * unit_price), 2) AS revenue,
        window_start,
        window_end
    FROM TABLE(CUMULATE(TABLE orders, DESCRIPTOR(ts), INTERVAL '5' MINUTES, INTERVAL '10' MINUTES))
    GROUP BY window_start, window_end;
