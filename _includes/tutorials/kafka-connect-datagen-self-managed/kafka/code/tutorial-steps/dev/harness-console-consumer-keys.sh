docker-compose exec connect kafka-console-avro-consumer \
 --bootstrap-server broker:9092 \
 --topic pageviews \
 --property print.key=true \
 --property key.separator=" : " \
 --max-messages 10
