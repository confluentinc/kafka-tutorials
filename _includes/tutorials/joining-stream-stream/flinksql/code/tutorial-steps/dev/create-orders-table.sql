CREATE TABLE orders (
    id INT, 
    total_amount DOUBLE, 
    customer_name VARCHAR,
    order_ts_raw BIGINT
) WITH (
    'connector' = 'kafka',
    'topic' = 'orders',
    'properties.bootstrap.servers' = 'broker:29092',
    'scan.startup.mode' = 'earliest-offset',
    'key.format' = 'raw',
    'key.fields' = 'id',
    'value.format' = 'json',
    'value.fields-include' = 'ALL'
);
