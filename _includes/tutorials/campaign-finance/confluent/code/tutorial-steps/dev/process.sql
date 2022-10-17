SET 'auto.offset.reset' = 'earliest';

-- Create a stream of campaign contributions
CREATE STREAM campaign_finance (
  time BIGINT,
  candidate_id VARCHAR,
  party_affiliation VARCHAR,
  contribution BIGINT
) WITH (
  KAFKA_TOPIC = 'campaign_finance',
  PARTITIONS = 1,
  VALUE_FORMAT = 'JSON'
);

-- Categorize contributions by amount
CREATE STREAM categorization_donations WITH (KAFKA_TOPIC = 'categorization_donations') AS
SELECT
  FORMAT_TIMESTAMP(FROM_UNIXTIME(time), 'yyyy-MM-dd HH:mm:ss') AS ts,
  party_affiliation,
  candidate_id,
  CASE
    WHEN contribution < 500 THEN 'small'
    WHEN contribution < 2900 THEN 'medium'
    ELSE 'large'
  END AS category
FROM campaign_finance
EMIT CHANGES;

-- Get count of "small" contributions
CREATE TABLE contributions_small_count WITH (KAFKA_TOPIC = 'contributions_small_count', KEY_FORMAT='JSON') AS
SELECT
  category,
  party_affiliation,
  COUNT(category) AS count_contributions
FROM categorization_donations
WHERE category = 'small'
GROUP BY category, party_affiliation;
