docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic movie-ticket-sales --broker-list broker:9092 --property value.schema="$(< src/main/avro/ticket-sale.avsc)"
