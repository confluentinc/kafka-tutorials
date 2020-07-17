CREATE STREAM all_publications (author VARCHAR, title VARCHAR)
    WITH (kafka_topic = 'publication_events', partitions = 1, key = 'author', value_format = 'avro');
