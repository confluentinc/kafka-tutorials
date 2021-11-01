docker run -v $PWD/configuration/ccloud.properties:/etc/ccloud.properties confluentinc/cp-server:6.2.1 /usr/bin/kafka-producer-perf-test \
    --topic topic-perf \
    --num-records 5000 \
    --record-size 5000 \
    --throughput -1 \
    --producer.config /etc/ccloud.properties
