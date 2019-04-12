docker-compose exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic user-events --broker-list broker:9092 --property value.schema="$(< src/main/avro/user.avsc)"
