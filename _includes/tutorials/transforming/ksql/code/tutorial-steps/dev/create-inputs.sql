CREATE STREAM raw_movies (ROWKEY INT KEY, id INT, title VARCHAR, genre VARCHAR)
    WITH (kafka_topic='movies', partitions=1, key='id', value_format = 'avro');
