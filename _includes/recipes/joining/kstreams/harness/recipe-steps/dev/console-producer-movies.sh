docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic movies --broker-list broker:9092 --property value.schema="$(< src/main/avro/movie.avsc)"
