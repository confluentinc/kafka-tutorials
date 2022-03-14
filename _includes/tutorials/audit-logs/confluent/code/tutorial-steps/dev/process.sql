SET 'auto.offset.reset' = 'earliest';

CREATE STREAM audit_log_events (
  id VARCHAR, 
  source VARCHAR, 
  specversion VARCHAR, 
  type VARCHAR, 
  time VARCHAR,  
  datacontenttype VARCHAR, 
  subject VARCHAR, 
  confluentRouting STRUCT<route VARCHAR>,	
  data STRUCT<
    serviceName VARCHAR, 
    methodName VARCHAR, 
    resourceName VARCHAR, 
    authenticationInfo STRUCT<principal VARCHAR>, 
    authorizationInfo STRUCT<
        granted BOOLEAN,
        operation VARCHAR,
        resourceType VARCHAR,
        resourceName VARCHAR,
        patternType VARCHAR,
        superUserAuthorization BOOLEAN
        >,
    request STRUCT<
        correlation_id VARCHAR,
        client_id VARCHAR
        >,
    requestMetadata STRUCT<client_address VARCHAR>
    >
) WITH (
  KAFKA_TOPIC = 'confluent-audit-log-events', 
  VALUE_FORMAT = 'JSON', 
  TIMESTAMP = 'time', 
  TIMESTAMP_FORMAT = 'yyyy-MM-dd''T''HH:mm:ss.SSSX',
  PARTITIONS = 6
);


-- Application logic
CREATE STREAM audit_log_topics WITH (KAFKA_TOPIC = 'topic-operations-audit-log', PARTITIONS = 6) AS 
SELECT
 time,
 data
FROM  audit_log_events
WHERE data->authorizationinfo->resourcetype = 'Topic'
EMIT CHANGES;
