CREATE TABLE ratings_by_6hr_window (
    title STRING,
    rating_count BIGINT,
    avg_rating DOUBLE,
    window_start TIMESTAMP(3),
    window_end TIMESTAMP(3)
) WITH (
    'connector' = 'kafka',
    'topic' = 'ratings-by-6hr-window',
    'properties.bootstrap.servers' = 'broker:9092',
    'scan.startup.mode' = 'earliest-offset',
    'key.format' = 'avro-confluent',
    'key.avro-confluent.url' = 'http://schema-registry:8081',
    'key.fields' = 'title;window_start;window_end',
    'value.format' = 'avro-confluent',
    'value.avro-confluent.url' = 'http://schema-registry:8081',
    'value.fields-include' = 'ALL'
)