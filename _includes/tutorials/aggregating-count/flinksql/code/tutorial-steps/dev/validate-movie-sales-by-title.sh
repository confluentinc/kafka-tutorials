docker exec -it confluent-cli confluent kafka topic consume movie-ticket-sales-by-title \
  --from-beginning \
  --print-key \
  --delimiter "-" \
  --bootstrap broker:9092 \
  --protocol PLAINTEXT \
  --schema-registry-endpoint http://schema-registry:8081 \
  --value-format avro
