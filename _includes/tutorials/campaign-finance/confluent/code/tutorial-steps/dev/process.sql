SET 'auto.offset.reset' = 'earliest';

-- Create a Stream for the campaign finance data
CREATE STREAM campaign_finance (
  time BIGINT,
  cand_id VARCHAR,
  party_affiliation VARCHAR,
  contribution BIGINT
) WITH (
  KAFKA_TOPIC = 'campaign_finance',
  PARTITIONS = 6,
  VALUE_FORMAT = 'JSON'
);

-- Filter by party affiliation
CREATE STREAM contributions_for_independents WITH (KAFKA_TOPIC = 'contributions_for_independents') AS
SELECT
  FORMAT_TIMESTAMP(FROM_UNIXTIME(time), 'yyyy-MM-dd HH:mm:ss') AS ts,
  cand_id,
  contribution
FROM campaign_finance
WHERE party_affiliation = 'IND';

-- Categorize donations by amount
CREATE STREAM categorization_donations WITH (KAFKA_TOPIC = 'categorization_donations') AS
SELECT
  FORMAT_TIMESTAMP(FROM_UNIXTIME(time), 'yyyy-MM-dd HH:mm:ss') AS ts,
  cand_id,
  CASE
    WHEN contribution < 500 THEN 'small'
    WHEN contribution < 2900 THEN 'medium'
    ELSE 'large'
  END AS category
FROM campaign_finance
EMIT CHANGES;

-- Get count of "small" contributions
CREATE TABLE contributions_small_count WITH (KAFKA_TOPIC = 'contributions_small_count') AS
SELECT
  category,
  COUNT(category) as count_contributions
FROM categorization_donations
WHERE category = 'small'
GROUP BY category;
