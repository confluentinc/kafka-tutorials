docker run --name tutorial-producer\
 --network=tutorial\
 -e "BOOTSTRAP_SERVERS=broker:9092"\
 -e "SCHEMA_REGISTRY_URL=http://schema-registry:8081"\
 -it io.confluent.developer/scala-producer:0.1.0-SNAPSHOT\
 "'Noam Wasserman','The Founder's dilemmas',Tech,400,2012-12-01"
