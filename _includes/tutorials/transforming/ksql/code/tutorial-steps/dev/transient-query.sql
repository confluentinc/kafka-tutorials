SELECT id, split(title, '::')[0] as title, split(title, '::')[1] AS year, genre FROM raw_movies LIMIT 4;
