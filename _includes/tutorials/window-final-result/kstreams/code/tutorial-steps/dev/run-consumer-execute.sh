docker exec -it broker /usr/bin/kafka-console-consumer --topic output-topic --bootstrap-server broker:9092\
 --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer\
 --from-beginning | grep -v "org.apache.kafka"