CREATE TABLE post_code_tab (post_code VARCHAR PRIMARY KEY, locality VARCHAR, state VARCHAR,
                            long DOUBLE, lat DOUBLE)
       WITH (kafka_topic='post_office_data', value_format='avro', partitions=1);
