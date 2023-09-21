CREATE TABLE deduplicated_clicks (
    ip_address VARCHAR, 
    url VARCHAR,
    click_ts VARCHAR,
    PRIMARY KEY (ip_address) NOT ENFORCED
) WITH (
    'connector' = 'upsert-kafka',
    'topic' = 'deduplicated_clicks',
    'properties.bootstrap.servers' = 'broker:29092',
    'key.format' = 'raw',
    'value.format' = 'json',
    'value.fields-include' = 'EXCEPT_KEY'
);
