CREATE TABLE suspicious_names (CREATED_DATE VARCHAR,
                               COMPANY_NAME VARCHAR PRIMARY KEY,
                               COMPANY_ID INT)
    WITH (kafka_topic='suspicious-accounts', partitions=1, value_format='JSON');
