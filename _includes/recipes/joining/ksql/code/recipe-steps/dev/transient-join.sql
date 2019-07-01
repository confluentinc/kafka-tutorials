SELECT ratings.id AS ID, title, release_year, rating FROM ratings LEFT JOIN movies ON ratings.id = movies.id LIMIT 9;
