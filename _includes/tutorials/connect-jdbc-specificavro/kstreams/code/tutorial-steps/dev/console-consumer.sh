docker exec -it schema-registry /usr/bin/kafka-avro-console-consumer --topic cities --bootstrap-server broker:9092 --from-beginning --property schema.registry.url=localhost:8082
