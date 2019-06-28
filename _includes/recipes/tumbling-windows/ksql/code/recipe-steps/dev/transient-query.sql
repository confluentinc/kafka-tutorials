CREATE TABLE best_ratings AS
    SELECT title,
           MAX(rating) AS best_rating,
           WINDOWSTART() AS window_start,
           WINDOWEND() AS window_end
    FROM ratings
    WINDOW TUMBLING (SIZE 6 HOURS)
    GROUP BY title;
