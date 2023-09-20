docker exec -it broker /usr/bin/kafka-console-consumer\
 --topic deduplicated_clicks\
 --bootstrap-server broker:9092 \
 --from-beginning \
 --max-messages 3