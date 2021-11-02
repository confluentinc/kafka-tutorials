docker run -v $PWD/configuration/ccloud.properties:/etc/ccloud.properties confluentinc/cp-server:6.2.1 /usr/bin/kafka-consumer-perf-test \
    --topic topic-perf \
    --messages 10000 \
    --bootstrap-server $(grep bootstrap.servers $PWD/configuration/ccloud.properties) \
    --consumer.config /etc/ccloud.properties \
    --consumer-props \
        fetch.min.bytes=100000
