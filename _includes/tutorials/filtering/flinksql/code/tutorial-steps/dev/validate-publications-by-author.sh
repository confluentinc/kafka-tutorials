docker exec -it broker /usr/bin/kafka-console-consumer\
  --topic george_martin_books \
  --from-beginning \
  --max-messages 4 \
  --timeout-ms 10000 \
  --bootstrap-server broker:9092 \
  --property key.deserializer=org.apache.kafka.common.serialization.IntegerDeserializer \
  --property print.key=true \
  --property key.separator="-"
