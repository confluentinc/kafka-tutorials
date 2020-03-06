CREATE TABLE movies (ROWKEY INT KEY, title VARCHAR, release_year INT)
    WITH (kafka_topic='movies', partitions=1, value_format='avro');
