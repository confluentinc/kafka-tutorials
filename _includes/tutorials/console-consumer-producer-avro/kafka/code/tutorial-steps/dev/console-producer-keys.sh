kafka-avro-console-producer --topic example-topic-avro --bootstrap-server broker:9092 \
 --property key.schema='{"type":"string"}' \
 --property value.schema="$(< /opt/app/schema/order_detail.avsc)" \
 --property parse.key=true \
 --property key.separator=":"