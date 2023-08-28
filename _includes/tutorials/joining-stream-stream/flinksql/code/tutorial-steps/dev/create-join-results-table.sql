CREATE TABLE shipped_orders (
    order_ts VARCHAR, 
    total DOUBLE, 
    customer VARCHAR,
    ship_id VARCHAR,
    ship_ts VARCHAR,
    warehouse VARCHAR,
    hours_to_ship INT
) WITH (
    'connector' = 'kafka',
    'topic' = 'shipped_orders',
    'properties.bootstrap.servers' = 'broker:29092',
    'scan.startup.mode' = 'earliest-offset',
    'key.format' = 'raw',
    'key.fields' = 'ship_id',
    'value.format' = 'json',
    'value.fields-include' = 'ALL'
);
