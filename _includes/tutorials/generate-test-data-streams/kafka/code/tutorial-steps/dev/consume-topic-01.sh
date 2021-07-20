docker exec -i kafkacat kafkacat -b broker:9092 -t clicks \
            -s value=avro -r http://schema-registry:8081        \
            -u -f 'Key     (%K bytes):\t%k\nPayload (%S bytes):\t%s\n--\n' \
            -C -c5 -o end