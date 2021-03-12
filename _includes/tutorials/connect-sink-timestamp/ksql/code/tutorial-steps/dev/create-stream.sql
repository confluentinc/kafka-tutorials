CREATE STREAM TEMPERATURE_READINGS_RAW (eventTime BIGINT, temperature int)
    WITH (kafka_topic='deviceEvents', value_format='avro', partitions=1);
