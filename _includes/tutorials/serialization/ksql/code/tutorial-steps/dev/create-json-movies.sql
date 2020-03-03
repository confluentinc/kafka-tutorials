CREATE STREAM movies_json (ROWKEY BIGINT KEY, title VARCHAR, release_year INT)
    WITH (KAFKA_TOPIC='json-movies',
          PARTITIONS=1,
          VALUE_FORMAT='json');
