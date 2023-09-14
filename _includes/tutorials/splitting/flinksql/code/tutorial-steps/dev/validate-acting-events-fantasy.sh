docker exec -e SCHEMA_REGISTRY_LOG4J_OPTS=" " -it schema-registry /usr/bin/kafka-avro-console-consumer \
  --topic acting-events-fantasy \
  --from-beginning \
  --max-messages 4 \
  --timeout-ms 10000 \
  --bootstrap-server broker:9092 \

