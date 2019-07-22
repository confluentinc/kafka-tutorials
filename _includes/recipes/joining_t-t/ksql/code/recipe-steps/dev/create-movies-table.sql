CREATE TABLE movies (id INT, title VARCHAR, release_year INT)
             WITH (KAFKA_TOPIC='movies', 
                   PARTITIONS=1, 
                   VALUE_FORMAT='Avro');
