CREATE TABLE movie_sales_by_year (
    release_year INT,
    min_total_sales INT,
    max_total_sales INT,
    PRIMARY KEY (release_year) NOT ENFORCED
) WITH (
    'connector' = 'upsert-kafka',
    'topic' = 'movie-sales-by-year',
    'properties.bootstrap.servers' = 'broker:9092',
    'key.format' = 'raw',
    'value.format' = 'avro-confluent',
    'value.avro-confluent.url' = 'http://schema-registry:8081',
    'value.fields-include' = 'EXCEPT_KEY'
);
