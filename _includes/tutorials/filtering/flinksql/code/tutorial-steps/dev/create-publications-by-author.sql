CREATE TABLE george_martin_books (
    book_id INT,
    title STRING
) WITH (
    'connector' = 'kafka',
    'topic' = 'george_martin_books',
    'properties.bootstrap.servers' = 'broker:9092',
    'scan.startup.mode' = 'earliest-offset',
    'key.format' = 'raw',
    'key.fields' = 'book_id',
    'value.format' = 'json',
    'value.fields-include' = 'EXCEPT_KEY'
);