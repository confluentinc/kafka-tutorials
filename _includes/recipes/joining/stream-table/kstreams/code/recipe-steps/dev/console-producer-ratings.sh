docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic ratings --broker-list broker:9092 --property value.schema="$(< src/main/avro/rating.avsc)"
