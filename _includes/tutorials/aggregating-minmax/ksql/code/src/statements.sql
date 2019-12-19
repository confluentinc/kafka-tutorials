CREATE STREAM MOVIE_SALES (title VARCHAR, release_year INT, total_sales INT)
    WITH (KAFKA_TOPIC='movie-ticket-sales',
          PARTITIONS=1,
          VALUE_FORMAT='avro');

CREATE TABLE MOVIE_FIGURES_BY_YEAR AS
    SELECT RELEASE_YEAR,
           MIN(TOTAL_SALES) AS MIN__TOTAL_SALES,
           MAX(TOTAL_SALES) AS MAX__TOTAL_SALES
    FROM MOVIE_SALES
    GROUP BY RELEASE_YEAR;
