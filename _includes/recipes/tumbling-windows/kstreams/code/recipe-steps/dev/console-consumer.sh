docker exec -it schema-registry /usr/bin/kafka-avro-console-consumer --topic rating-counts --bootstrap-server broker:9092 --from-beginning

docker exec -it broker /usr/bin/kafka-console-consumer --topic rating-counts --bootstrap-server broker:9092 --from-beginning
