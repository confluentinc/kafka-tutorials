CREATE STREAM PURCHASE_HISTORY_STREAM AS
  SELECT FIRST_NAME,
       LAST_NAME,
       CURRENT_PURCHASE - PREVIOUS_PURCHASE as PURCHASE_DIFF
FROM PURCHASE_STREAM;