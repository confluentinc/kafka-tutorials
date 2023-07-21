confluent kafka topic produce app-one-topic \
       --parse-key \
       --value-format avro \
       --schema src/main/avro/login-event.avsc
