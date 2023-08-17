CREATE TABLE orders (
    id INT, 
    total_amount DOUBLE, 
    customer_name VARCHAR,
    order_ts BIGINT,
    order_ts_ltz as TO_TIMESTAMP_LTZ(order_ts, 3)
) WITH (
    'connector' = 'kafka',
    'topic' = 'orders',
    'properties.bootstrap.servers' = 'broker:9092',
    'scan.startup.mode' = 'earliest-offset',
    'key.format' = 'raw',
    'key.fields' = 'id',
    'value.format' = 'json',
    'value.fields-include' = 'ALL'
);
