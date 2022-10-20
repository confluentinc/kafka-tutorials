CREATE TABLE suspicious_names (CREATED_TS VARCHAR,
                               COMPANY_NAME VARCHAR PRIMARY KEY,
                               COMPANY_ID INT)
    WITH (kafka_topic='suspicious_names',
          partitions=1,
          value_format='JSON',
          timestamp='CREATED_TS',
          timestamp_format = 'yyyy-MM-dd HH:mm:ss');
