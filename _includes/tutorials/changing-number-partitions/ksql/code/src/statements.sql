CREATE STREAM s1 WITH (kafka_topic = 'topic1');

CREATE STREAM s2 WITH (kafka_topic = 'topic2', partitions = 8);
