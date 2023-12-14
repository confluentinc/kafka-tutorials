docker exec -i broker kafka-console-producer --bootstrap-server broker:29092 --topic topic1 --property parse.key=true --property key.separator=,
