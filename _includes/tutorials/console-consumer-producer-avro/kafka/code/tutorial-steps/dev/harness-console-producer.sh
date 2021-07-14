docker-compose exec -T schema-registry kafka-avro-console-producer --topic example-topic-avro\
  --bootstrap-server broker:9092 \
  --property value.schema="$(< schema/order_detail.avsc)"
