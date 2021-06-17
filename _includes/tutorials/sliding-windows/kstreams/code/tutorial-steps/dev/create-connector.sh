curl -i -X PUT http://localhost:8083/connectors/datagen_local_01/config \
     -H "Content-Type: application/json" \
     -d '{
            "connector.class": "io.confluent.kafka.connect.datagen.DatagenConnector",
            "key.converter": "org.apache.kafka.connect.storage.StringConverter",
            "kafka.topic": "temp-readings",
            "schema.filename": "/schemas/datagen-temperature-reading.avsc",
            "schema.keyfield": "device_id",
            "max.interval": 300,
            "iterations": 10000000,
            "tasks.max": "1"
        }'
