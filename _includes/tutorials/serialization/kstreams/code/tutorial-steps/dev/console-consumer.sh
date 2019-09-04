docker exec -it schema-registry /usr/bin/kafka-avro-console-consumer --topic avro-movies --bootstrap-server broker:29092 --from-beginning --property value.schema="$(< src/main/avro/movie.avsc)"
