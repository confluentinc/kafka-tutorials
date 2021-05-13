docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic movie-ticket-sales --bootstrap-server broker:9092 --property value.schema="$(< src/main/avro/movie-ticket-sales.avsc)"
