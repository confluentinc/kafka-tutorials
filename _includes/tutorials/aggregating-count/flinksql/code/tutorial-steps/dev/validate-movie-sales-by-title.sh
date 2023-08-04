docker exec -it confluent-cli confluent kafka topic consume movie-ticket-sales-by-title \
  --bootstrap broker:9092 \
  --protocol PLAINTEXT \
  --from-beginning \
  --print-key \
  --delimiter "-" \
  --value-format avro \
  --schema-registry-endpoint http://schema-registry:8081
