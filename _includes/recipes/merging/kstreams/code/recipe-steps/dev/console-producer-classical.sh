docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic classical-song-events --broker-list broker:9092 --property value.schema="$(< src/main/avro/song_event.avsc)"
