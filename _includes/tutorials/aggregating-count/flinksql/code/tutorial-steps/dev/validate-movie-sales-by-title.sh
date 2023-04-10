docker exec -e SCHEMA_REGISTRY_LOG4J_OPTS=" " -it schema-registry /usr/bin/kafka-avro-console-consumer \
  --topic movie-ticket-sales-by-title \
  --from-beginning \
  --max-messages 9 \
  --timeout-ms 10000 \
  --bootstrap-server broker:9092 \
  --property key.deserializer=org.apache.kafka.common.serialization.IntegerDeserializer \
  --property print.key=true \
  --property key.separator="-"
