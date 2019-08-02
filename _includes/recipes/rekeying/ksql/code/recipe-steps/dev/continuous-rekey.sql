CREATE STREAM RATINGS_REKEYED 
    WITH (KAFKA_TOPIC='ratings_keyed_by_id') AS
    SELECT * 
    FROM RATINGS
    PARTITION BY ID;
