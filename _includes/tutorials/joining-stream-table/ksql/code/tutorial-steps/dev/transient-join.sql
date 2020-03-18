SELECT ratings.rowkey AS ID, title, release_year, rating FROM ratings LEFT JOIN movies ON ratings.rowkey = movies.rowkey EMIT CHANGES LIMIT 9;
