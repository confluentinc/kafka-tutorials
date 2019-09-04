docker exec -it broker /usr/bin/kafka-console-consumer --topic rating-counts --bootstrap-server broker:29092 --from-beginning --property print.key=true
