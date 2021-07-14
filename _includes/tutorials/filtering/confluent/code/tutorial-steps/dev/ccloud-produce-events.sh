ccloud kafka topic produce publications \
  --parse-key \
  --delimiter ":" \
  --value-format avro \
  --schema src/main/avro/publication.avsc
