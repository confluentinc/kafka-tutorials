docker exec -it schema-registry /usr/bin/kafka-avro-console-consumer --topic products-filtered --bootstrap-server broker:9092 --property value.schema="$(< src/main/avro/product.avsc)"
