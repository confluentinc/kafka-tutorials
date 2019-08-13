SELECT
    order_id, order_ts, total_amount, customer_name,
    shipment_id, shipment_ts, warehouse, ship_time
FROM ORDERS_SHIPMENTS
LIMIT 3;
