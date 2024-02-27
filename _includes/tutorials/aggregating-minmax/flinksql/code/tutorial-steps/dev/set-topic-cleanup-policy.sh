docker exec -it confluent-cli confluent kafka topic update movie-sales-by-year \
  --config cleanup.policy=compact \
  --url http://rest-proxy:8082 \
  --no-authentication
