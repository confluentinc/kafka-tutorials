CREATE TABLE lead_actor (ROWKEY VARCHAR PRIMARY KEY, title VARCHAR, actor_name VARCHAR)
             WITH (KAFKA_TOPIC='lead_actors', 
                   PARTITIONS=1, 
                   VALUE_FORMAT='avro');
