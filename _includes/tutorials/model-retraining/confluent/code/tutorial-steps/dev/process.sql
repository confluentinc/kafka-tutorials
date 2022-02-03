SET 'auto.offset.reset' = 'earliest';

-- Create stream of predictions
CREATE STREAM predicted_weight(
  "Fish_Id" VARCHAR KEY,
  "Species" VARCHAR,
  "Height" DOUBLE,
  "Length" DOUBLE,
  "Prediction" DOUBLE
  )
WITH(
  KAFKA_TOPIC = 'kt.mdb.weight-prediction', 
  VALUE_FORMAT = 'JSON',
  PARTITIONS = 6
);

-- Create stream of actual weights
CREATE STREAM actual_weight(
  "Fish_Id" VARCHAR KEY,
  "Species" VARCHAR,
  "Weight" DOUBLE
  )
WITH(
  KAFKA_TOPIC = 'kt.mdb.machine-weight', 
  VALUE_FORMAT = 'JSON',
  PARTITIONS = 6
);

-- Create stream joining predictions with actual weights
CREATE STREAM diff_weight
WITH(
  KAFKA_TOPIC = 'diff_weight', 
  VALUE_FORMAT = 'JSON'
)
AS SELECT
   -- This fake Key field will give us something to group by in the next step
   'Key' AS "Key", 
   predicted_weight."Fish_Id" AS "Fish_Id",
   predicted_weight."Species" AS "Species",
   predicted_weight."Length" AS "Length",
   predicted_weight."Height" AS "Height",
   predicted_weight."Prediction" AS "PredictedWeight",
   actual_weight."Weight" AS "ActualWeight",
   ROUND(ABS(predicted_weight."Prediction" - actual_weight."Weight") / actual_weight."Weight", 3) AS "Error"
FROM predicted_weight
INNER JOIN actual_weight
WITHIN 1 MINUTE
GRACE PERIOD 1 MINUTE
ON predicted_weight."Fish_Id" = actual_weight."Fish_Id";

-- Create table of one minute aggregates with over 15% error rate
CREATE TABLE retrain_weight
WITH(
  KAFKA_TOPIC = 'retrain_weight', 
  VALUE_FORMAT = 'JSON'
)
AS SELECT
   "Key",
   COLLECT_SET("Species") AS "Species",
   EARLIEST_BY_OFFSET("Fish_Id") AS "Fish_Id_Start",
   LATEST_BY_OFFSET("Fish_Id") AS "Fish_Id_End",
   AVG("Error") AS "ErrorAvg"
FROM DIFF_WEIGHT
WINDOW TUMBLING (SIZE 1 MINUTE, GRACE PERIOD 1 MINUTE)
GROUP BY "Key"
HAVING ROUND(AVG(DIFF_WEIGHT.`Error`), 2) > 0.15;
