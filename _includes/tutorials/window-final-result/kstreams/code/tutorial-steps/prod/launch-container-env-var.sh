docker run\
 -e "BOOTSTRAP_SERVERS=broker:9092"\
 -e "SCHEMA_REGISTRY_URL=http://schema-registry:8081"\
 io.confluent.developer/kstreams-filter:0.0.1-SNAPSHOT
