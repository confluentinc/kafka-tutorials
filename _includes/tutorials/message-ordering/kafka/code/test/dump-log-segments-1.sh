docker-compose exec broker kafka-run-class kafka.tools.DumpLogSegments \
  --print-data-log \ 
  --files /var/lib/kafka/data/myTopic-1/00000000000000000000.log \
  --deep-iteration
