docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic clicks --broker-list broker:9092 --property value.schema="$(< src/main/avro/click.avsc)"
