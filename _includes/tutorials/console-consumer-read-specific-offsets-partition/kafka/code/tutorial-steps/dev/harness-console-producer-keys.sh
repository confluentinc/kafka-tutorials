docker exec -T broker kafka-console-producer --topic example-topic --bootstrap-server broker:9092 \
  --property parse.key=true \
  --property key.separator=":"