SET 'auto.offset.reset' = 'earliest';

-- stream of user clicks:
CREATE STREAM clickstream (
  _time BIGINT,
  time VARCHAR,
  ip VARCHAR,
  request VARCHAR,
  status INT,
  userid INT,
  bytes BIGINT,
  agent VARCHAR
) WITH (
  KAFKA_TOPIC = 'clickstream',
  VALUE_FORMAT = 'JSON',
  PARTITIONS = 1
);

-- users lookup table:
CREATE TABLE web_users (
  user_id VARCHAR PRIMARY KEY,
  registered_At BIGINT,
  username VARCHAR,
  first_name VARCHAR,
  last_name VARCHAR,
  city VARCHAR,
  level VARCHAR
) WITH (
  KAFKA_TOPIC = 'clickstream_users',
  VALUE_FORMAT = 'JSON',
  PARTITIONS = 1
);

-- Build materialized stream views:

-- enrich click-stream with more user information:
CREATE STREAM user_clickstream AS
  SELECT
    u.user_id,
    u.username,
    ip,
    u.city,
    request,
    status,
    bytes
  FROM clickstream c
  LEFT JOIN web_users u ON cast(c.userid AS VARCHAR) = u.user_id;

-- Build materialized table views:

-- Table of html pages per minute for each user:
CREATE TABLE pages_per_min AS
  SELECT
    userid AS k1,
    AS_VALUE(userid) AS userid,
    WINDOWSTART AS EVENT_TS,
    COUNT(*) AS pages
  FROM clickstream WINDOW HOPPING (SIZE 60 SECOND, ADVANCE BY 5 SECOND)
  WHERE request LIKE '%html%'
  GROUP BY userid;

-- User sessions table - 30 seconds of inactivity expires the session
-- Table counts number of events within the session
CREATE TABLE click_user_sessions AS
  SELECT
    username AS K,
    AS_VALUE(username) AS username,
    WINDOWEND AS EVENT_TS,
    COUNT(*) AS events
  FROM user_clickstream WINDOW SESSION (30 SECOND)
  GROUP BY username;

-- number of errors per min, using 'HAVING' Filter to show ERROR codes > 400
-- where count > 5
CREATE TABLE errors_per_min_alert WITH (KAFKA_TOPIC = 'errors_per_min_alert') AS
  SELECT
    status AS k1,
    AS_VALUE(status) AS status,
    WINDOWSTART AS EVENT_TS,
    COUNT(*) AS errors
  FROM clickstream WINDOW HOPPING (SIZE 60 SECOND, ADVANCE BY 20 SECOND)
  WHERE status > 400
  GROUP BY status
  HAVING COUNT(*) > 5 AND COUNT(*) IS NOT NULL;

-- Enriched user details table:
-- Aggregate (count&groupBy) using a TABLE-Window
CREATE TABLE user_ip_activity WITH (KEY_FORMAT = 'JSON', KAFKA_TOPIC = 'user_ip_activity') AS
  SELECT
    username AS k1,
    ip AS k2,
    city AS k3,
    AS_VALUE(username) AS username,
    WINDOWSTART AS EVENT_TS,
    AS_VALUE(ip) AS ip,
    AS_VALUE(city) AS city,
    COUNT(*) AS count
  FROM user_clickstream WINDOW TUMBLING (SIZE 60 SECOND)
  GROUP BY username, ip, city
  HAVING COUNT(*) > 1;
