CREATE TABLE CITIES (ROWKEY INT PRIMARY KEY) WITH (KAFKA_TOPIC='postgres_cities', VALUE_FORMAT='AVRO');
