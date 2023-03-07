docker exec -it broker /usr/bin/kafka-configs \
  --bootstrap-server localhost:29092 \
  --entity-type topics \
  --entity-name movie-sales-by-year \
  --alter \
  --add-config cleanup.policy=compact
