docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic all-publications --broker-list broker:9092 --property value.schema="$(< src/main/avro/publication.avsc)"
