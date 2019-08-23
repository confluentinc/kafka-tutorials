CREATE STREAM MOVIE_SALES (title VARCHAR, release_year INT, total_sales INT)
    WITH (KAFKA_TOPIC='movie-ticket-sales',
          PARTITIONS=1,
          VALUE_FORMAT='avro');
