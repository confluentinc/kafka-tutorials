CREATE TABLE post_code_tab (post_code VARCHAR PRIMARY KEY, locality VARCHAR, state VARCHAR,
                            long DOUBLE, lat DOUBLE)
       WITH (kafka_topic='POST_CODE', value_format='avro', partitions=1);
