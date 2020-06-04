CREATE STREAM movies_json (MOVIE_ID BIGINT KEY, title VARCHAR, release_year INT)
    WITH (KAFKA_TOPIC='json-movies',
          PARTITIONS=1,
          VALUE_FORMAT='json');

CREATE STREAM movies_avro
    WITH (KAFKA_TOPIC='avro-movies', VALUE_FORMAT='avro') AS
    SELECT * FROM movies_json;
