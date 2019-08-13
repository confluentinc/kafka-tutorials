CREATE STREAM raw_movies (id int, title varchar, genre varchar)
    WITH (kafka_topic='movies', partitions=1, key='id', value_format = 'avro');
