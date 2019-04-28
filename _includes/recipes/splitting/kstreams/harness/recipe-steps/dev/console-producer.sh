docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic all-geo-events --broker-list broker:9092 --property value.schema="$(< src/main/avro/user.avsc)"
