docker exec -it schema-registry /usr/bin/kafka-avro-console-consumer \
  --topic movie-sales-by-year \
  --from-beginning \
  --max-messages 9 \
  --timeout-ms 10000 \
  --property schema.registry.url=http://schema-registry:8081 \
  --bootstrap-server broker:9092 \
  --property key.deserializer=org.apache.kafka.common.serialization.IntegerDeserializer \
  --property print.key=true \
  --property key.separator="-"
