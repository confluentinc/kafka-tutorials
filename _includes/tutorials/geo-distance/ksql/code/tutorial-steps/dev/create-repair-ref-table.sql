CREATE TABLE repair_center_tab (repair_state VARCHAR PRIMARY KEY, long DOUBLE, lat DOUBLE)
             WITH (kafka_topic='REPAIR_center', value_format='avro', partitions=1);

