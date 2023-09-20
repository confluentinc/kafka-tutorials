CREATE TABLE ratings_by_6hr_window
WITH (
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
) AS
    SELECT title,
       COUNT(*) AS rating_count,
       AVG(rating) AS avg_rating,
       window_start,
       window_end
    FROM TABLE(TUMBLE(TABLE ratings, DESCRIPTOR(ts), INTERVAL '6' HOURS))
    GROUP BY title, window_start, window_end;
