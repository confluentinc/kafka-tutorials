select o.id as order_id,
           FROM_UNIXTIME(o.order_ts_raw) as order_timestamp,
           o.total_amount,
           o.customer_name,
           s.id as shipment_id,
           FROM_UNIXTIME(s.ship_ts_raw) as shipment_timestamp,
           s.warehouse    
    from orders o inner join shipments s
    on o.id = s.order_id
    and TO_TIMESTAMP(FROM_UNIXTIME(s.ship_ts_raw)) 
     between TO_TIMESTAMP(FROM_UNIXTIME(o.order_ts_raw)) 
     and TO_TIMESTAMP(FROM_UNIXTIME(o.order_ts_raw))  + INTERVAL '8' HOUR;
