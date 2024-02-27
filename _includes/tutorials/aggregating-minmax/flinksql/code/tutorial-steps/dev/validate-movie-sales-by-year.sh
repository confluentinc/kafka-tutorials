docker exec -it confluent-cli confluent kafka topic consume movie-sales-by-year \
  --bootstrap broker:9092 \
  --protocol PLAINTEXT \
  --from-beginning \
  --print-key \
  --key-format integer \
  --delimiter "-" \
  --value-format avro \
  --schema-registry-endpoint http://schema-registry:8081
