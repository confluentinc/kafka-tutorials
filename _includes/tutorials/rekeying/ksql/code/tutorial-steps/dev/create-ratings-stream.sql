CREATE STREAM ratings (old INT KEY, id INT, rating DOUBLE)
    WITH (kafka_topic='ratings', 
          partitions=2, 
          value_format='avro');
