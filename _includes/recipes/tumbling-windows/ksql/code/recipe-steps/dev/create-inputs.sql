CREATE STREAM ratings (title VARCHAR, release_year INT, rating DOUBLE)
    WITH (kafka_topic='ratings', partitions=1, value_format='avro');
