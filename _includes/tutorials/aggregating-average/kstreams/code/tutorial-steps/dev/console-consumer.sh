docker exec -it broker /usr/bin/kafka-console-consumer --topic rating-averages --bootstrap-server broker:9092 \
  --property "print.key=true"\
  --property "key.deserializer=org.apache.kafka.common.serialization.LongDeserializer" \
  --property "value.deserializer=org.apache.kafka.common.serialization.DoubleDeserializer" \
  --from-beginning
