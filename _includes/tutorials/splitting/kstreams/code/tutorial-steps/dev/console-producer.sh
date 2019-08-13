docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic acting-events --broker-list broker:9092 --property value.schema="$(< src/main/avro/acting_event.avsc)"
