curl -i -X PUT http://localhost:8083/connectors/datagen_local_01/config \
     -H "Content-Type: application/json" \
     -d '{
            "connector.class": "io.confluent.kafka.connect.datagen.DatagenConnector",
            "key.converter": "org.apache.kafka.connect.storage.StringConverter",
            "kafka.topic": "login-events",
            "schema.filename": "/schemas/datagen-logintime.avsc",
            "schema.keyfield": "userid",
            "max.interval": 1000,
            "iterations": 10000000,
            "tasks.max": "1"
        }'
