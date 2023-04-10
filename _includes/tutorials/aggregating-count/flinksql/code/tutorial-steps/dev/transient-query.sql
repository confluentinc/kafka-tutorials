SELECT title,
       COUNT(ticket_total_value) AS tickets_sold
FROM movie_ticket_sales
GROUP BY title;
