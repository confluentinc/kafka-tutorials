SET 'auto.offset.reset' = 'earliest';

-- Register the stream of SFDC CDC Opportunities
CREATE STREAM stream_sfdc_cdc_opportunity_raw
WITH (
  KAFKA_TOPIC = 'sfdc.cdc.raw',
  VALUE_FORMAT = 'AVRO',
  PARTITIONS = 6
);

-- Create a new stream with Replay ID and Change Event Header for just Gap Events
CREATE STREAM stream_sfdc_cdc_opportunity_change_log AS
  SELECT
    STREAM_SFDC_CDC_OPPORTUNITY_RAW.REPLAYID AS REPLAYID,
    STREAM_SFDC_CDC_OPPORTUNITY_RAW.CHANGEEVENTHEADER AS CHANGEEVENTHEADER
  FROM stream_sfdc_cdc_opportunity_raw
  WHERE UCASE(STREAM_SFDC_CDC_OPPORTUNITY_RAW.CHANGEEVENTHEADER->CHANGETYPE) LIKE 'GAP%'
  EMIT CHANGES;
