docker exec -it confluent-cli confluent kafka topic update movie-ticket-sales-by-title \
  --config cleanup.policy=compact \
  --url http://rest-proxy:8082 \
  --no-authentication
