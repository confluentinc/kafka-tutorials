docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic raw-movies --broker-list broker:9092 --property value.schema="$(< src/main/avro/input_movie_event.avsc)"
