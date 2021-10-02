kafka-avro-console-consumer \
  --topic orders-avro \
  --property schema.registry.url=http://localhost:8081 \
  --bootstrap-server broker:9092 \
  --property print.key=true \
  --property key.separator="-" \
  --from-beginning
