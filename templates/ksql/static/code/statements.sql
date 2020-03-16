CREATE STREAM events (event_type VARCHAR, name VARCHAR)
    WITH (kafka_topic = 'events', 
    	partitions = 1, 
    	key = 'event_type', 
    	value_format = 'avro');


CREATE STREAM sport_events WITH (kafka_topic = 'sport_events', partitions = 1) AS
    SELECT event_type, name
    FROM events
    WHERE event_type = 'sport';
