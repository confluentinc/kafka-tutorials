CREATE TABLE movie_ticket_sales (
    title STRING,
    sales_ts STRING,
    total_ticket_value INT
) WITH (
    'connector' = 'kafka',
    'topic' = 'movie-ticket-sales',
    'properties.bootstrap.servers' = 'broker:9092',
    'scan.startup.mode' = 'earliest-offset',
    'key.format' = 'raw',
    'key.fields' = 'title',
    'value.format' = 'avro-confluent',
    'value.avro-confluent.url' = 'http://schema-registry:8081',
    'value.fields-include' = 'EXCEPT_KEY'
);
