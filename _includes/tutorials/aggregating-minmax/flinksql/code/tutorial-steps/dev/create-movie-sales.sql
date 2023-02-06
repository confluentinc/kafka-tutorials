CREATE TABLE movie_sales (
    id INT,
    title STRING,
    release_year INT,
    total_sales INT
) WITH (
    'connector' = 'kafka',
    'topic' = 'movie-sales',
    'properties.bootstrap.servers' = 'broker:9092',
    'scan.startup.mode' = 'earliest-offset',
    'key.format' = 'raw',
    'key.fields' = 'id',
    'value.format' = 'avro-confluent',
    'value.avro-confluent.url' = 'http://schema-registry:8081',
    'value.fields-include' = 'EXCEPT_KEY'
);
