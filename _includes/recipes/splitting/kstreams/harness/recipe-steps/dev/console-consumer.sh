docker exec -it schema-registry /usr/bin/kafka-avro-console-consumer --topic filtered-user-events --bootstrap-server broker:9092 --from-beginning
