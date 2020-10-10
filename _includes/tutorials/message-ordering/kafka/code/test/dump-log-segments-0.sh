docker-compose exec broker kafka-run-class kafka.tools.DumpLogSegments \
--print-data-log \ 
--files /var/log/topic1-0/00000000000000000000.log \
--deep-iteration
