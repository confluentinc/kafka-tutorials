CREATE STREAM TEMPERATURE_READINGS_EVENTTIME (temperature BIGINT, eventTime BIGINT)
    WITH (KAFKA_TOPIC='deviceEvents',
          VALUE_FORMAT='avro',
          TIMESTAMP='eventTime');
