SELECT title,
       rating_count,
       TIMESTAMPTOSTRING(window_start, 'yyy-MM-dd HH:mm:ss'),
       TIMESTAMPTOSTRING(window_end, 'yyy-MM-dd HH:mm:ss')
FROM rating_count
LIMIT 11;
