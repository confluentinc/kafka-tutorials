INSERT INTO acting_events_fantasy
    SELECT name, title
    FROM acting_events
    WHERE genre = 'fantasy';
