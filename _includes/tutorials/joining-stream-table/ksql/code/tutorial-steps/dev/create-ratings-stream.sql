CREATE STREAM ratings (ROWKEY INT KEY, rating DOUBLE)
    WITH (kafka_topic='ratings', partitions=1, value_format='avro');
