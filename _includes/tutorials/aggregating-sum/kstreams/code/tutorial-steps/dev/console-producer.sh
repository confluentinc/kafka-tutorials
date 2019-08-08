docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic movie-ticket-sales --broker-list broker:29092 --property value.schema="$(< src/main/avro/ticketsale.avsc)"
