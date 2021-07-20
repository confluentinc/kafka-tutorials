ccloud kafka topic produce movie-ticket-sales \
  --parse-key \
  --delimiter ":" \
  --value-format avro \
  --schema src/main/avro/ticket-sale.avsc