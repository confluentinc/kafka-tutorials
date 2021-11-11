confluent kafka topic produce input \
  --parse-key \
  --delimiter ":" \
  --value-format avro \
  --schema src/main/avro/order.avsc
