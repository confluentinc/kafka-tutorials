CREATE STREAM ratings (id INT, rating DOUBLE)
    WITH (kafka_topic='ratings', partitions=1, value_format='avro');
