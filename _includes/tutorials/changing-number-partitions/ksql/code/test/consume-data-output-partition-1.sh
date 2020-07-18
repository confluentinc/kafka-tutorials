docker-compose exec broker kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic topic2 \
  --property print.key=true \
  --property key.separator=, \
  --partition 1 \
  --from-beginning
