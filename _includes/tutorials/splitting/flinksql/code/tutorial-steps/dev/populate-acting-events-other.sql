INSERT INTO acting_events_other
    SELECT name, title
    FROM acting_events
    WHERE genre <> 'drama' AND genre <> 'fantasy';
