SELECT *
FROM Orders o, Shipments s
WHERE o.id = s.order_id
AND o.order_time BETWEEN s.ship_time - INTERVAL '4' HOUR AND s.ship_time



SELECT O.ID AS ORDER_ID,
           TIMESTAMP(O.ORDER_TS) AS ORDER_TS,
           O.TOTAL_AMOUNT,
           O.CUSTOMER_NAME,
           S.ID AS SHIPMENT_ID,
           TIMESTAMP(S.SHIP_TS) AS SHIPMENT_TS,
           S.WAREHOUSE,
           (S.ROWTIME - O.ROWTIME) / 1000 / 60 AS SHIP_TIME
    FROM ORDERS O INNER JOIN SHIPMENTS S
    WITHIN 7 DAYS
    ON O.ID = S.ORDER_ID;

    AND o.order_time BETWEEN s.ship_time - INTERVAL '4' HOUR AND s.ship_time