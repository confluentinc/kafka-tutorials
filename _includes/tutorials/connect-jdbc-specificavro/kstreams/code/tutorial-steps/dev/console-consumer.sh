docker exec -it schema-registry /usr/bin/kafka-avro-console-consumer --topic cities --bootstrap-server broker:29092 --from-beginning --property schema.registry.url=http://localhost:8081 --property print.key=true --property key.deserializer=org.apache.kafka.common.serialization.LongDeserializer

docker exec -it schema-registry /usr/bin/kafka-avro-console-consumer --topic cities_keyed --bootstrap-server broker:29092 --from-beginning --property schema.registry.url=http://localhost:8081 --property print.key=true --property key.deserializer=org.apache.kafka.common.serialization.LongDeserializer
