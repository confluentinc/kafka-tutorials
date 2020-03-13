CREATE STREAM sport_events WITH (kafka_topic = 'sport_events', partitions = 1) AS
    SELECT event_type, name
    FROM events
    WHERE event_type = 'sport';
