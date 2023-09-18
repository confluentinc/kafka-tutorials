INSERT INTO george_martin_books
SELECT
      book_id,
      title
FROM publication_events
WHERE author = 'George R. R. Martin';