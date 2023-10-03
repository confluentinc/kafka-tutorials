docker exec -e SCHEMA_REGISTRY_LOG4J_OPTS=" " -it schema-registry /usr/bin/kafka-avro-console-consumer \
  --topic revenue-per-hour-cumulating \
  --from-beginning \
  --max-messages 5 \
  --timeout-ms 10000 \
  --bootstrap-server broker:9092
