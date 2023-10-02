docker exec -e SCHEMA_REGISTRY_LOG4J_OPTS=" " -it schema-registry /usr/bin/kafka-avro-console-consumer \
  --topic ratings-by-6hr-window \
  --from-beginning \
  --max-messages 7 \
  --timeout-ms 10000 \
  --bootstrap-server broker:9092
