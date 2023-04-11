docker exec -it broker /usr/bin/kafka-configs \
  --bootstrap-server localhost:29092 \
  --entity-type topics \
  --entity-name movie-ticket-sales-by-title \
  --alter \
  --add-config cleanup.policy=compact
