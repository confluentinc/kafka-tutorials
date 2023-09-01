# 1. Here we have to pint the event times to ensure the same result
# 2. The produce use here doesn't run in background like in the tutorial

function produce () { echo $1 | docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --bootstrap-server broker:9092 --topic input-topic --property value.schema="$(< src/main/avro/pressure-alert.avsc)"; }

produce '{"id":"101","datetime":"2023-09-21T05:45:02.+0200","pressure":30}'
produce '{"id":"101","datetime":"2023-09-21T05:45:02.+0200","pressure":30}'
produce '{"id":"101","datetime":"2023-09-21T05:45:02.+0200","pressure":30}'
produce '{"id":"102","datetime":"2023-09-21T05:45:02.+0200","pressure":30}'
produce '{"id":"101","datetime":"2023-09-21T05:45:02.+0200","pressure":30}'
produce '{"id":"101","datetime":"2023-09-21T05:45:07.+0200","pressure":30}'
produce '{"id":"101","datetime":"2023-09-21T05:44:13.+0200","pressure":30}'
produce '{"id":"102","datetime":"2023-09-21T05:45:13.+0200","pressure":30}'
produce '{"id":"102","datetime":"2023-09-21T05:43:23.+0200","pressure":30}'
produce '{"id":"301","datetime":"2023-09-21T12:45:23.+0900","pressure":30}'
produce '{"id":"301","datetime":"2023-09-21T12:45:24.+0900","pressure":30}'
produce '{"id":"XXX","datetime":"2023-09-21T06:00:00.+0200","pressure":30}'