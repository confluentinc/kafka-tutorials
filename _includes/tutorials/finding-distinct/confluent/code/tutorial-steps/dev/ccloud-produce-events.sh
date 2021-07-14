ccloud kafka topic produce clicks \
  --parse-key \
  --delimiter ":" \
  --value-format avro \
  --schema src/main/avro/click.avsc
