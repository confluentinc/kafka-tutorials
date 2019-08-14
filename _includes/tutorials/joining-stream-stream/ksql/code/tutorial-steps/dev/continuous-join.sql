CREATE STREAM shipped_orders AS
    SELECT o.order_id AS order_id,
           TIMESTAMPTOSTRING(o.rowtime, 'yyyy-MM-dd HH:mm:ss') AS order_ts,
           o.total_amount,
           o.customer_name,
           s.shipment_id,
           TIMESTAMPTOSTRING(s.rowtime, 'yyyy-MM-dd HH:mm:ss') AS shipment_ts,
           s.warehouse, (s.rowtime - o.rowtime) / 1000 / 60 AS ship_time
    FROM orders o INNER JOIN shipments s
    WITHIN 7 DAYS
    ON o.order_id = s.order_id;
