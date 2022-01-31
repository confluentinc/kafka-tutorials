CREATE STREAM flight_updates (ID          INT KEY
                            , FLIGHT_ID   INT
                            , UPDATED_DEP TIMESTAMP
                            , REASON      VARCHAR
                             )
              WITH (KAFKA_TOPIC='flight_updates'
                   , FORMAT='AVRO'
                   , PARTITIONS=6);
