CREATE TABLE movie_ticket_sales_by_title (
    title STRING,
    tickets_sold INT
    PRIMARY KEY (title NOT ENFORCED)
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




