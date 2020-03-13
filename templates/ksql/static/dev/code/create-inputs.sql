CREATE STREAM events (event_type VARCHAR, name VARCHAR)
    WITH (kafka_topic = 'events', partitions = 1, key = 'event_type', value_format = 'avro');
