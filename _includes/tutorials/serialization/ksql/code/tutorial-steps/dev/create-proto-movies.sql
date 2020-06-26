CREATE STREAM movies_proto
    WITH (KAFKA_TOPIC='proto-movies', VALUE_FORMAT='protobuf') AS
    SELECT
        ROWKEY as MOVIE_ID,
        TITLE,
        RELEASE_YEAR
    FROM movies_avro;
