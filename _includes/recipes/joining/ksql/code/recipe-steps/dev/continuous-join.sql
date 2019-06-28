CREATE STREAM rated_movies
    WITH (kafka_topic='rated_movies',
          partitions=1,
          value_format='avro') AS
    SELECT ratings.id, title, rating
    FROM ratings
    LEFT JOIN movies ON ratings.id = movies.id;
