CREATE STREAM CLICKS (IP_ADDRESS VARCHAR, URL VARCHAR, TIMESTAMP VARCHAR)
    WITH (KAFKA_TOPIC = 'CLICKS',
          FORMAT = 'JSON',
          TIMESTAMP = 'TIMESTAMP',
          TIMESTAMP_FORMAT = 'yyyy-MM-dd''T''HH:mm:ssXXX',
          PARTITIONS = 1);
