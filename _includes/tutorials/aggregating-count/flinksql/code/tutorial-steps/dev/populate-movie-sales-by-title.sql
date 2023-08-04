INSERT INTO movie_ticket_sales_by_title
SELECT title,
       COUNT(total_ticket_value) AS tickets_sold
FROM movie_ticket_sales
GROUP BY title;
