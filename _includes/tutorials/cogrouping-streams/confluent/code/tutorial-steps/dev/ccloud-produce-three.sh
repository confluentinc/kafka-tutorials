confluent kafka topic produce app-three-topic \
       --parse-key \
       --value-format avro \
       --schema src/main/avro/login-event.avsc