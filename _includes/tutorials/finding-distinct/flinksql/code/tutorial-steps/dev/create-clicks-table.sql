CREATE TABLE clicks (
    ip_address VARCHAR, 
    url VARCHAR,
    click_ts_raw BIGINT
) WITH (
    'connector' = 'kafka',
    'topic' = 'clicks',
    'properties.bootstrap.servers' = 'broker:29092',
    'scan.startup.mode' = 'earliest-offset',
    'key.format' = 'raw',
    'key.fields' = 'id',
    'value.format' = 'json',
    'value.fields-include' = 'ALL'
);
