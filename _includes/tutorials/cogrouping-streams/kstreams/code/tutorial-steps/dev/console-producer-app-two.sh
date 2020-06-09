docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic app-two-topic --broker-list broker:9092\
  --property "parse.key=true"\
  --property 'key.schema={"type":"string"}'\
  --property "key.separator=:"\
  --property value.schema="$(< src/main/avro/login-event.avsc)"