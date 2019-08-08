docker exec -it broker /usr/bin/kafka-console-consumer --topic MOVIE_REVENUE --bootstrap-server broker:29092 --from-beginning --property print.key=true
