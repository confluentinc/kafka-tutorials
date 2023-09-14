CREATE TABLE acting_events_other
WITH (
    'connector' = 'kafka',
    'topic' = 'acting-events-other',
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
    WHERE genre <> 'drama' AND genre <> 'fantasy';
