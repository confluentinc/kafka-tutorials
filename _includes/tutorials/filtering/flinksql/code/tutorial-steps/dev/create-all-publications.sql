CREATE TABLE publication_events (
    book_id INT,
    author STRING,
    title STRING    
) WITH (
    'connector' = 'kafka',
    'topic' = 'publication_events',
    'properties.bootstrap.servers' = 'broker:9092',
    'scan.startup.mode' = 'earliest-offset',
    'key.format' = 'raw',
    'key.fields' = 'book_id',
    'value.format' = 'json',
    'value.fields-include' = 'EXCEPT_KEY'
);