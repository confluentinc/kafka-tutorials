docker exec -t broker kafka-console-consumer --bootstrap-server broker:29092 --topic topic2 --property print.key=true --property key.separator=, --partition 1 --from-beginning  --max-messages 3
