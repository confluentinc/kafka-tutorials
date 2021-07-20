tr '\n' ' ' < src/statements.sql | \
sed 's/;/;\'$'\n''/g' | \
while read stmt; do
    jq --arg value1 "$stmt" -n '{ "ksql" : $value1 }'| \
        curl -s -X "POST" "http://localhost:8088/ksql" \
             -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
             -d @- | \
        jq
done