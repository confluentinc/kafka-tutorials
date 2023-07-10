SET 'auto.offset.reset' = 'earliest';

-- Create Table of Survey Respondents
CREATE TABLE SURVEY_RESPONDENTS (
    RESPONDENT_ID VARCHAR PRIMARY KEY, 
    NAME VARCHAR,
    TEAM VARCHAR, 
    EMAIL VARCHAR, 
    ADDRESS VARCHAR
) WITH (
    KAFKA_TOPIC = 'survey-respondents', 
    VALUE_FORMAT = 'JSON', 
    KEY_FORMAT = 'KAFKA',
    PARTITIONS = 6
);

-- Create Survey Responses Stream
CREATE STREAM SURVEY_RESPONSES ( 
    SURVEY_ID VARCHAR KEY,
    RESPONDENT_ID VARCHAR,
    SURVEY_QUESTION VARCHAR,
    SURVEY_RESPONSE VARCHAR
) WITH (
    KAFKA_TOPIC = 'survey-responses', 
    VALUE_FORMAT = 'JSON', 
    KEY_FORMAT = 'KAFKA',
    PARTITIONS = 6
);