CREATE TABLE movie_ticket_sales_by_title (
    title STRING,
    sales_ts STRING,
    total_ticket_value INT
    PRIMARY KEY (title)
) WITH (
    'connector' = 'upsert-kafka',
    'topic' = 'movie-ticket-sales-by-title',
    'properties.bootstrap.servers' = 'broker:9092',
    'key.format' = 'raw',
    'key.fields' = 'title',
    'value.format' = 'avro-confluent',
    'value.avro-confluent.url' = 'http://schema-registry:8081',
    'value.fields-include' = 'EXCEPT_KEY'
);




