kafka-avro-console-producer --topic example-topic-avro --bootstrap-server broker:9092 --property value.schema="$(< /opt/app/schema/order_detail.avsc)"
