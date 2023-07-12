confluent kafka topic produce app-two-topic \
       --parse-key \
       --value-format avro \
       --schema src/main/avro/login-event.avsc