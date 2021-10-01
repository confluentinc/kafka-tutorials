kafka-avro-console-producer \
  --topic example-topic \
  --bootstrap-server broker:9092 \
  --property schema.registry.url=http://localhost:8081
