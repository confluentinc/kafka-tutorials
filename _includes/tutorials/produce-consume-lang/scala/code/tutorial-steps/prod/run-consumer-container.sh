docker run -d --name tutorial-consumer\
 -p 8888:8888\
 --network=tutorial\
 -e "HTTP_PORT=8888"\
 -e "CONSUMER_GRP=production-app"\
 -e "BOOTSTRAP_SERVERS=broker:9092"\
 -e "SCHEMA_REGISTRY_URL=http://schema-registry:8081"\
 io.confluent.developer/scala-consumer:0.1.0-SNAPSHOT