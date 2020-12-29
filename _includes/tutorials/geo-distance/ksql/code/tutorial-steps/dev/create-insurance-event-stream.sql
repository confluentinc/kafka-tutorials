CREATE STREAM insurance_event_stream (customer_name VARCHAR, phone_model VARCHAR, event VARCHAR,
                                      post_code VARCHAR)
       WITH (kafka_topic='phone_event_raw', value_format='avro', partitions=1);

