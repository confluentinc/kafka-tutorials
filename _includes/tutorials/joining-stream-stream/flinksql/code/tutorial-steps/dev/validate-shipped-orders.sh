docker exec -it broker /usr/bin/kafka-console-consumer\
 --topic shipped_orders\
 --bootstrap-server broker:9092 \
 --from-beginning \
 --max-messages 10