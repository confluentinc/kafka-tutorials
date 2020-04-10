docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic input --broker-list broker:9092\
  --property "parse.key=true"\
  --property 'key.schema={"type":"long"}'\
  --property "key.separator=:"\
  --property value.schema="$(< src/main/avro/order.avsc)"