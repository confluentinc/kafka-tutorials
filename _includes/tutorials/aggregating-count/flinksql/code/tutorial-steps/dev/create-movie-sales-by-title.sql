CREATE TABLE movie_ticket_sales_by_title (
    title STRING,
    tickets_sold BIGINT,
    PRIMARY KEY (title) NOT ENFORCED
) WITH (
    'connector' = 'upsert-kafka',
    'topic' = 'movie-ticket-sales-by-title',
    'properties.bootstrap.servers' = 'broker:9092',
    'key.format' = 'raw',
    'value.format' = 'avro-confluent',
    'value.avro-confluent.url' = 'http://schema-registry:8081',
    'value.fields-include' = 'EXCEPT_KEY'
);
