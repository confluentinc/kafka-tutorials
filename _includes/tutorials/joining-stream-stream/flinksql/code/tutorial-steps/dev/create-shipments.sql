CREATE TABLE shipments (
    id VARCHAR, 
    order_id INT, 
    warehouse VARCHAR,
    ship_ts BIGINT,
    ship_ts_ltz as TO_TIMESTAMP_LTZ(ship_ts, 3)
) WITH (
    'connector' = 'kafka',
    'topic' = 'shipments',
    'properties.bootstrap.servers' = 'broker:9092',
    'scan.startup.mode' = 'earliest-offset',
    'key.format' = 'raw',
    'key.fields' = 'id',
    'value.format' = 'json',
    'value.fields-include' = 'ALL'
);
