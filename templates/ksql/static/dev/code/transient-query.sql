SELECT event_type, name FROM events WHERE event_type = 'sport' EMIT CHANGES LIMIT 2;
