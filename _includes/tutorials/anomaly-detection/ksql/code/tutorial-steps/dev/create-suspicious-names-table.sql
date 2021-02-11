CREATE TABLE suspicious_names (CREATED_DATE VARCHAR,
                               COMPANY_NAME VARCHAR PRIMARY KEY,
                               COMPANY_ID INT)
    WITH (kafka_topic='suspicious_names',
          partitions=1,
          value_format='JSON',
          timestamp='CREATED_DATE',
          timestamp_format='yyyy-MM-dd HH:mm:ss');
