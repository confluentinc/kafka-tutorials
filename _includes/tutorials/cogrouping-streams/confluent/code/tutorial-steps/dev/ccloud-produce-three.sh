ccloud kafka topic produce app-three-topic \
       --parse-key \
       --delimiter ":" \
       --value-format avro \
       --schema src/main/avro/login-event.avsc
