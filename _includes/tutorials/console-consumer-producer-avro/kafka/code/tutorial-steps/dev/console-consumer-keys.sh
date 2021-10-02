kafka-avro-console-consumer \
  --topic orders-avro \
  --bootstrap-server broker:9092 \
  --property schema.registry.url=http://localhost:8081 \
  --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
  --property print.key=true \
  --property key.separator="-" \
  --from-beginning
