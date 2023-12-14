docker exec -t broker kafka-console-consumer \
  --bootstrap-server broker:29092 \
  --topic topic1 \
  --property print.key=true \
  --property key.separator=, \
  --partition 0 \
  --from-beginning
