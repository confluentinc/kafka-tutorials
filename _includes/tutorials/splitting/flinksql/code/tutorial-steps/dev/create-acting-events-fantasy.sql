CREATE TABLE acting_events_fantasy
WITH (
    'connector' = 'kafka',
    'topic' = 'acting-events-fantasy',
    'properties.bootstrap.servers' = 'broker:9092',
    'scan.startup.mode' = 'earliest-offset',
    'key.format' = 'avro-confluent',
    'key.avro-confluent.url' = 'http://schema-registry:8081',
    'key.fields' = 'name;title',
    'value.format' = 'avro-confluent',
    'value.avro-confluent.url' = 'http://schema-registry:8081',
    'value.fields-include' = 'ALL'
) AS
    SELECT name, title
    FROM acting_events
    WHERE genre = 'fantasy';
