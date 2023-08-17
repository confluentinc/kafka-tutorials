select o.id as order_id,
           o.order_ts_ltz,
           o.total_amount,
           o.customer_name,
           s.id as shipment_id,
           s.ship_ts_ltz,
           s.warehouse,
           (s.ship_ts - o.order_ts) / 1000 / 60 as shipment_time      
    from orders o inner join shipments s
    on o.id = s.order_id
    and o.order_ts_ltz between o.order_ts_ltz - INTERVAL '1' DAY and s.ship_ts_ltz;