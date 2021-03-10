#!/bin/bash

# Wait for the datagen topic to be created
#while : 
#  do output=$(docker-compose exec broker kafka-topics --bootstrap-server localhost:29092 --topic login-events --describe)
#  if [[ "$output" =~ "Topic: login-events" ]]; then
#    echo "Topic login-events exists"
#    break
#  else
#    echo "Connect logs: "
#    docker-compose logs connect
#  fi
#  sleep 5 
#done


# Wait for the datagen topic to be created
sleep 20

echo "avsc:"
docker-compose exec connect ls /schemas/

echo "connect logs:"
docker-compose logs connect

output=$(docker-compose exec broker kafka-topics --bootstrap-server localhost:29092 --topic login-events --describe)
echo "$output"
if [[ "$output" =~ "Topic: login-events" ]]; then
  echo "Topic login-events exists"
else
  echo "Topic does not exist"
fi
