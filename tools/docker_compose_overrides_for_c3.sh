#!/bin/sh

TOOLS_HOME=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
KT_HOME=$(dirname "${TOOLS_HOME}")

DOCKER_COMPOSE_OVERRIDE=docker-compose.override.yml

cat <<EOF > $DOCKER_COMPOSE_OVERRIDE
version: "2"

services:

  control-center:
    image: confluentinc/cp-enterprise-control-center:5.5.0
    hostname: control-center
    container_name: control-center
    ports:
      - "9021:9021"
    environment:
      CONTROL_CENTER_BOOTSTRAP_SERVERS: 'broker:9092'
      CONTROL_CENTER_ZOOKEEPER_CONNECT: 'zookeeper:2181'
      CONTROL_CENTER_CONNECT_CLUSTER: 'connect:8083'
      CONTROL_CENTER_KSQL_KSQLDB1_URL: "http://ksqldb-server:8088"
      CONTROL_CENTER_KSQL_KSQLDB1_ADVERTISED_URL: "http://localhost:8088"
      CONTROL_CENTER_SCHEMA_REGISTRY_URL: "http://schema-registry:8081"
      CONTROL_CENTER_REPLICATION_FACTOR: 1
      CONTROL_CENTER_INTERNAL_TOPICS_PARTITIONS: 1
      CONTROL_CENTER_MONITORING_INTERCEPTOR_TOPIC_PARTITIONS: 1
      CONFLUENT_METRICS_TOPIC_REPLICATION: 1
      PORT: 9021

  broker:
    image: confluentinc/cp-server:5.5.0
    environment:
      KAFKA_METRIC_REPORTERS: io.confluent.metrics.reporter.ConfluentMetricsReporter
      CONFLUENT_METRICS_REPORTER_BOOTSTRAP_SERVERS: broker:9092
      CONFLUENT_METRICS_REPORTER_ZOOKEEPER_CONNECT: zookeeper:2181
      CONFLUENT_METRICS_REPORTER_TOPIC_REPLICAS: 1
      CONFLUENT_METRICS_ENABLE: 'true'
      CONFLUENT_SUPPORT_CUSTOMER_ID: 'anonymous'
EOF

for instance in `cd $KT_HOME && find . -name docker-compose.yml`; do
  DESTINATION="$( cd "$( dirname "${KT_HOME}/${instance}" )" >/dev/null && pwd )"
  yes | cp $DOCKER_COMPOSE_OVERRIDE $DESTINATION
done
