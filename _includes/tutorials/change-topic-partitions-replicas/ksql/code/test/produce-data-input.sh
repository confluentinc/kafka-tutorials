docker exec -i broker kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic topic1 \
  --property parse.key=true \
  --property key.separator=,
