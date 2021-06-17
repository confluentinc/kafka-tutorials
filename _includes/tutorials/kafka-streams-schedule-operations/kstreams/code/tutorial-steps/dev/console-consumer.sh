docker-compose exec broker kafka-console-consumer \
 --bootstrap-server broker:9092 \
 --topic output-topic \
 --property print.key=true \
 --value-deserializer "org.apache.kafka.common.serialization.LongDeserializer" \
 --property key.separator=" : "  \
 --from-beginning \
 --max-messages 10

