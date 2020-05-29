CREATE STREAM movies_avro (ROWKEY BIGINT KEY, title VARCHAR, release_year INT)
    WITH (KAFKA_TOPIC='avro-movies',
          PARTITIONS=1,
          VALUE_FORMAT='avro');

CREATE STREAM movies_proto
    WITH (KAFKA_TOPIC='proto-movies', VALUE_FORMAT='protobuf') AS
    SELECT
        ROWKEY as MOVIE_ID,
        TITLE,
        RELEASE_YEAR
    FROM movies_avro;
