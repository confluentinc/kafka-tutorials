INSERT INTO movie_sales_by_title
SELECT title,
       COUNT(ticket_total_value) AS TICKETS_SOLD
FROM movie_ticket_sales
GROUP BY title;

