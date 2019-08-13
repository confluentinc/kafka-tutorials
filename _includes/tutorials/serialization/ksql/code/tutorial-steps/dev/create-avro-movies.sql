CREATE STREAM movies_avro
    WITH (KAFKA_TOPIC='avro-movies', VALUE_FORMAT='avro') AS
    SELECT * FROM movies_json;
