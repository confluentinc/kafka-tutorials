kafka-console-producer --topic input-topic --broker-list broker:9092\
  --property parse.key=true\
  --property key.separator=":"