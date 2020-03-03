CREATE STREAM movies_avro
    WITH (KAFKA_TOPIC='avro-movies', VALUE_FORMAT='avro') AS
    SELECT
        ROWKEY as MOVIE_ID,
        TITLE,
        RELEASE_YEAR
    FROM movies_json;
