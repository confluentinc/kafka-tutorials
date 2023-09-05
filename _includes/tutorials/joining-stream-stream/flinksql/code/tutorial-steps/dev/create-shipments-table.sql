CREATE TABLE shipments (
    id VARCHAR, 
    order_id INT, 
    warehouse VARCHAR,
    ship_ts_raw BIGINT
) WITH (
    'connector' = 'kafka',
    'topic' = 'shipments',
    'properties.bootstrap.servers' = 'broker:29092',
    'scan.startup.mode' = 'earliest-offset',
    'key.format' = 'raw',
    'key.fields' = 'id',
    'value.format' = 'json',
    'value.fields-include' = 'ALL'
);
