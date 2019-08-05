CREATE STREAM ratings (id INT, rating DOUBLE)
    WITH (kafka_topic='ratings', 
          partitions=2, 
          value_format='avro');
