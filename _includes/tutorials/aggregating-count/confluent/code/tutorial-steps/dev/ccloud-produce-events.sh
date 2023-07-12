confluent kafka topic produce movie-ticket-sales \
  --parse-key \
  --value-format avro \
  --schema src/main/avro/ticket-sale.avsc