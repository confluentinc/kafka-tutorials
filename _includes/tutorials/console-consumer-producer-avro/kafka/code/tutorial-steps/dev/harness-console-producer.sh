docker-compose exec -T broker kafka-console-producer \
  --topic orders-avro \
  --bootstrap-server broker:9092 \
  --property schema.registry.url=http://localhost:8081
